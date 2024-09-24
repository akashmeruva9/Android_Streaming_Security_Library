# AndroidArmour

## We have include the following 2 Streaming Android app security features in this respository :
- [DRM (Digital Rights Management)](#section-1) 
- [Content Access Control](#section-2)

# <a name="section-1">
## 1. DRM (Digital Rights Management):
To implement **DRM (Digital Rights Management)** for streaming apps in Android, you can use **Widevine DRM**, which is integrated with Android’s media framework. Widevine provides content protection for video and audio streams, ensuring that digital content is secure from piracy.

### Steps to Implement DRM for Streaming Apps:

### 1. **Set Up ExoPlayer for Streaming with DRM**

ExoPlayer is commonly used in Android for media playback, and it supports **Widevine DRM**.

#### **Step 1: Add ExoPlayer Dependency**
   - Add ExoPlayer to your `build.gradle` file.

```groovy
implementation 'com.google.android.exoplayer:exoplayer:2.18.1'
```

#### **Step 2: Configure ExoPlayer to Handle DRM-Protected Content**
   - Use `DefaultDrmSessionManager` to handle DRM licenses for Widevine content.

```kotlin
private fun initExoPlayer(context: Context, drmLicenseUrl: String, videoUrl: String) {
    val httpDataSourceFactory = DefaultHttpDataSource.Factory()

    // Set up DRM configuration for Widevine
    val drmLicenseDataSourceFactory = HttpMediaDrmCallback(drmLicenseUrl, httpDataSourceFactory)

    val drmSessionManager = DefaultDrmSessionManager.Builder()
        .setUuidAndExoMediaDrmProvider(C.WIDEVINE_UUID, FrameworkMediaDrm.DEFAULT_PROVIDER)
        .setMultiSession(false)
        .build(drmLicenseDataSourceFactory)

    // Prepare the media source with DRM
    val mediaSource = ProgressiveMediaSource.Factory(httpDataSourceFactory)
        .setDrmSessionManager(drmSessionManager)
        .createMediaSource(MediaItem.fromUri(videoUrl))

    val exoPlayer = ExoPlayer.Builder(context).build().apply {
        setMediaSource(mediaSource)
        prepare()
        playWhenReady = true
    }

    // Attach player to a view for playback
    playerView.player = exoPlayer
}
```

### 2. **Integrate DRM License Server**

- You will need a **DRM license server** that provides the decryption keys for the protected content. The `drmLicenseUrl` in the example points to your Widevine license server, which validates the playback rights and provides keys.

### 3. **Encrypt and Protect Content**

- Content providers must **encrypt** video and audio streams before distribution. Widevine uses **AES encryption** to protect content.
- The content is encrypted using a tool, and only authorized clients with valid DRM licenses can decrypt and play the content.

### 4. **Prevent Screen Recording (Optional)**

- For additional protection, you can block screen recording using `FLAG_SECURE` on your activity to prevent the user from recording video streams.

```kotlin
window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
```

### Steps Summary:
1. **ExoPlayer + Widevine DRM**: Use ExoPlayer and configure it with Widevine DRM to handle protected video/audio streams.
2. **License Server**: Use a DRM license server to validate users and issue decryption keys for encrypted streams.
3. **Content Encryption**: The streaming provider must encrypt content before distribution using Widevine tools.
4. **Prevent Screen Recording**: Add `FLAG_SECURE` to prevent unauthorized screen captures.
</a>

# <a name="section-2">

## 2. Content Access Control :

To implement **Content Access Control** in Android streaming apps, you can enforce **user-level restrictions** by managing user sessions and securing content using encryption. Here’s how to achieve this:

### 1. **User Authentication and Session Management**

#### **Step 1: Implement User Authentication**
   - Use **OAuth2**, **Firebase Authentication**, or any other authentication service to verify the user's identity. This helps ensure that only authorized users can access premium content.

```kotlin
fun authenticateUser(token: String) {
    // Simulating authentication via OAuth2 or Firebase
    if (isValidToken(token)) {
        // User authenticated
        loadContentForUser()
    } else {
        // Handle unauthorized access
        showUnauthorizedAccessError()
    }
}

fun isValidToken(token: String): Boolean {
    // Implement token validation with your backend
    return token == "VALID_USER_TOKEN"
}
```

#### **Step 2: Manage User Sessions**
   - After authentication, store session tokens securely using **EncryptedSharedPreferences** to persist the session across app usage.

##### **Add Dependency for EncryptedSharedPreferences:**
```groovy
dependencies {
    implementation "androidx.security:security-crypto:1.1.0-alpha05"
}
```

```kotlin
val sharedPreferences = EncryptedSharedPreferences.create(
    "user_session_prefs",
    MasterKey.Builder(applicationContext).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
    applicationContext,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)

fun saveUserSession(sessionToken: String) {
    sharedPreferences.edit().putString("SESSION_TOKEN", sessionToken).apply()
}

fun getUserSession(): String? {
    return sharedPreferences.getString("SESSION_TOKEN", null)
}
```

### 2. **Content Encryption for Premium Access**

#### **Step 1: Encrypt Premium Content**
   - Secure premium content (e.g., video or audio streams) by encrypting it. This ensures that even if someone accesses the content URL, they cannot play it without decryption.

   - Use **Widevine DRM** for premium video content or **AES encryption** for smaller files.

```kotlin
fun encryptContent(content: ByteArray, secretKey: SecretKey): ByteArray {
    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(Cipher.ENCRYPT_MODE, secretKey)
    return cipher.doFinal(content)
}
```

#### **Step 2: Decrypt Content for Authorized Users**
   - Only users with valid sessions and permissions can access the **decryption keys** from your server to decrypt the content.

```kotlin
fun decryptContent(encryptedContent: ByteArray, secretKey: SecretKey): ByteArray {
    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(Cipher.DECRYPT_MODE, secretKey)
    return cipher.doFinal(encryptedContent)
}
```

### 3. **Enforce User-Level Access Control**

#### **Step 1: Check User Permissions**
   - When a user tries to access premium content, check their subscription level or permissions from the backend. Deny access if they don’t have sufficient privileges.

```kotlin
fun checkUserPermissions(userId: String): Boolean {
    // Fetch user permissions from server
    return userHasPremiumAccess(userId)
}

fun userHasPremiumAccess(userId: String): Boolean {
    // Implement permission logic (e.g., via API)
    return true // Replace with actual logic
}
```

#### **Step 2: Block Unauthorized Users**
   - If a user is not authorized to access the content, block them and show an appropriate message.

```kotlin
if (!checkUserPermissions(userId)) {
    // Block access
    showUnauthorizedAccessError()
} else {
    // Grant access to content
    playPremiumContent()
}
```

### Steps Summary:
1. **User Authentication and Session Management**: Authenticate users and maintain session tokens securely using `EncryptedSharedPreferences`.
2. **Content Encryption**: Encrypt premium content using AES or Widevine DRM, ensuring only authorized users can decrypt and access it.
3. **Enforce User Permissions**: Check user permissions before granting access to premium content.

</a>
