package com.mobdeve.s17.itismob_mc0

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.mobdeve.s17.itismob_mc0.databinding.EditProfilePageBinding

class EditProfileActivity : ComponentActivity() {
    private lateinit var viewBinding: EditProfilePageBinding
    private val USER_PREFERENCE = "USER_PREFERENCE"
    private lateinit var sp: SharedPreferences
    private var userid: String? = null
    private var userName: String? = null
    private var userEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = EditProfilePageBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // Initialize SharedPreferences
        sp = getSharedPreferences(USER_PREFERENCE, MODE_PRIVATE)
        userid = sp.getString("userId", null)
        userName = sp.getString("userName", null)
        userEmail = sp.getString("userEmail", null)

        setupEditTextListeners()
        setupNavBar()

        // Set initial values
        viewBinding.nameEtv.setText(userName)
        viewBinding.emailEtv.setText(userEmail)

        viewBinding.epSaveChangesBtn.setOnClickListener {
            saveChanges()
        }

        // Initial button state
        checkIfChangesMade()
    }

    private fun saveChanges() {
        val name = viewBinding.nameEtv.text.toString().trim()
        val email = viewBinding.emailEtv.text.toString().trim()
        val oldPass = viewBinding.oldPassEtvp.text.toString().trim()
        val newPass = viewBinding.newPassEtvp.text.toString().trim()
        val confirmNewPass = viewBinding.confirmNewPassEtvp.text.toString().trim()

        val nameChanged = name != userName
        val emailChanged = email != userEmail
        val passwordFieldsFilled = oldPass.isNotEmpty() || newPass.isNotEmpty() || confirmNewPass.isNotEmpty()

        Log.d("DEBUG", "Name changed: $nameChanged, Email changed: $emailChanged, Password fields filled: $passwordFieldsFilled")

        when {
            // Both name/email and password changes
            (nameChanged || emailChanged) && passwordFieldsFilled -> {
                if (validatePasswordChange(oldPass, newPass, confirmNewPass)) {
                    updateProfileAndPassword(name, email, oldPass, newPass)
                }
            }

            // Only password change attempted
            passwordFieldsFilled && !nameChanged && !emailChanged -> {
                if (validatePasswordChange(oldPass, newPass, confirmNewPass)) {
                    updatePasswordOnly(oldPass, newPass)
                }
            }

            // Only name/email changes
            (nameChanged || emailChanged) && !passwordFieldsFilled -> {
                updateProfileOnly(name, email)
            }

            else -> {
                Toast.makeText(this, "No changes detected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updatePasswordOnly(oldPass: String, newPass: String) {
        if (userid.isNullOrEmpty()) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        DatabaseHelper.updateUserPassword(userid!!, oldPass, newPass) { success, errorMessage ->
            runOnUiThread {
                if (success) {
                    Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show()
                    clearPasswordFields()
                    checkIfChangesMade()
                } else {
                    when {
                        errorMessage?.contains("incorrect", ignoreCase = true) == true -> {
                            viewBinding.oldPassEtvp.error = "Incorrect current password"
                        }
                        errorMessage?.contains("not found", ignoreCase = true) == true -> {
                            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            viewBinding.newPassEtvp.error = "Update failed"
                            Toast.makeText(this, "Failed to update password. Please try again.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun updateProfileOnly(name: String, email: String) {
        if (userid.isNullOrEmpty()) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        val oldUsername = userName // Store the old username before updating

        DatabaseHelper.updateUserInfo(userid!!, name, email) { success, errorMessage ->
            runOnUiThread {
                if (success) {
                    // Check if username actually changed
                    if (oldUsername != name) {
                        Log.d("EditProfile", "Username changed from '$oldUsername' to '$name', updating across all data...")
                        updateUsernameAcrossAllData(oldUsername!!, name) { updateSuccess, updateError ->
                            runOnUiThread {
                                if (updateSuccess) {
                                    editLoginState(name, email)
                                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                                    Log.d("EditProfile", "Username updated across all data successfully")
                                } else {
                                    // Still update local state but show warning
                                    editLoginState(name, email)
                                    Toast.makeText(this,
                                        "Profile updated but some comments/ratings may show old username",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    Log.w("EditProfile", "Username update partially failed: $updateError")
                                }
                                checkIfChangesMade()
                            }
                        }
                    } else {
                        // Only email changed, no need to update comments/ratings
                        editLoginState(name, email)
                        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                        checkIfChangesMade()
                        Log.d("EditProfile", "Only email updated, username unchanged")
                    }
                } else {
                    Toast.makeText(this, errorMessage ?: "Failed to update profile", Toast.LENGTH_SHORT).show()
                    Log.e("EditProfile", "Failed to update user info: $errorMessage")
                }
            }
        }
    }

    private fun updateUsernameAcrossAllData(oldUsername: String, newUsername: String, onComplete: (Boolean, String?) -> Unit) {
        DatabaseHelper.updateUsernameAcrossAllData(oldUsername, newUsername) { success, errorMessage ->
            runOnUiThread {
                onComplete(success, errorMessage)
            }
        }
    }
    private fun updateProfileAndPassword(name: String, email: String, oldPass: String, newPass: String) {
        if (userid.isNullOrEmpty()) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        val oldUsername = userName // Store the old username before updating

        DatabaseHelper.updateUserPassword(userid!!, oldPass, newPass) { success, errorMessage ->
            runOnUiThread {
                if (success) {
                    DatabaseHelper.updateUserInfo(userid!!, name, email) { profileSuccess, profileError ->
                        runOnUiThread {
                            if (profileSuccess) {
                                // Check if username changed
                                if (oldUsername != name) {
                                    Log.d("EditProfile", "Username changed, updating across all data...")
                                    updateUsernameAcrossAllData(oldUsername!!, name) { updateSuccess, updateError ->
                                        runOnUiThread {
                                            if (updateSuccess) {
                                                editLoginState(name, email)
                                                Toast.makeText(this, "Profile and password updated successfully", Toast.LENGTH_SHORT).show()
                                                Log.d("EditProfile", "Complete update successful")
                                            } else {
                                                // Still update local state but show warning
                                                editLoginState(name, email)
                                                Toast.makeText(this,
                                                    "Profile and password updated but some comments/ratings may show old username",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                Log.w("EditProfile", "Username update partially failed: $updateError")
                                            }
                                            clearPasswordFields()
                                            checkIfChangesMade()
                                        }
                                    }
                                } else {
                                    // Only email changed, no need to update comments/ratings
                                    editLoginState(name, email)
                                    Toast.makeText(this, "Profile and password updated successfully", Toast.LENGTH_SHORT).show()
                                    clearPasswordFields()
                                    checkIfChangesMade()
                                    Log.d("EditProfile", "Password and email updated, username unchanged")
                                }
                            } else {
                                Toast.makeText(this, "Password updated but profile update failed: ${profileError ?: "Unknown error"}", Toast.LENGTH_SHORT).show()
                                Log.e("EditProfile", "Profile update failed after password change: $profileError")
                            }
                        }
                    }
                } else {
                    // Password update failed - don't update profile
                    when {
                        errorMessage?.contains("incorrect", ignoreCase = true) == true -> {
                            viewBinding.oldPassEtvp.error = "Incorrect current password"
                        }
                        errorMessage?.contains("not found", ignoreCase = true) == true -> {
                            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            viewBinding.newPassEtvp.error = "Update failed"
                            Toast.makeText(this, "Failed to update password. Please try again.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    Log.e("EditProfile", "Password update failed: $errorMessage")
                }
            }
        }
    }


    private fun setupEditTextListeners() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                checkIfChangesMade()
            }
        }

        viewBinding.nameEtv.addTextChangedListener(textWatcher)
        viewBinding.emailEtv.addTextChangedListener(textWatcher)
        viewBinding.oldPassEtvp.addTextChangedListener(textWatcher)
        viewBinding.newPassEtvp.addTextChangedListener(textWatcher)
        viewBinding.confirmNewPassEtvp.addTextChangedListener(textWatcher)
    }

    private fun checkIfChangesMade() {
        val name = viewBinding.nameEtv.text.toString().trim()
        val email = viewBinding.emailEtv.text.toString().trim()
        val oldPass = viewBinding.oldPassEtvp.text.toString().trim()
        val newPass = viewBinding.newPassEtvp.text.toString().trim()
        val confirmNewPass = viewBinding.confirmNewPassEtvp.text.toString().trim()

        val hasChanges = name != userName ||
                email != userEmail ||
                oldPass.isNotEmpty() ||
                newPass.isNotEmpty() ||
                confirmNewPass.isNotEmpty()

        viewBinding.epSaveChangesBtn.isEnabled = hasChanges
        viewBinding.epSaveChangesBtn.alpha = if (hasChanges) 1.0f else 0.5f
    }

    private fun validatePasswordChange(oldPass: String, newPass: String, confirmNewPass: String): Boolean {
        // Clear previous errors
        viewBinding.oldPassEtvp.error = null
        viewBinding.newPassEtvp.error = null
        viewBinding.confirmNewPassEtvp.error = null

        return when {
            oldPass.isEmpty() || newPass.isEmpty() || confirmNewPass.isEmpty() -> {
                Toast.makeText(this, "Please fill all password fields", Toast.LENGTH_SHORT).show()
                false
            }

            newPass.length < 8 -> {
                viewBinding.newPassEtvp.error = "Must be at least 8 characters"
                Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show()
                false
            }

            newPass != confirmNewPass -> {
                viewBinding.confirmNewPassEtvp.error = "Passwords do not match"
                Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show()
                false
            }

            oldPass == newPass -> {
                viewBinding.newPassEtvp.error = "New password must be different from old password"
                Toast.makeText(this, "New password must be different from old password", Toast.LENGTH_SHORT).show()
                false
            }

            else -> true
        }
    }

    private fun clearPasswordFields() {
        viewBinding.oldPassEtvp.text?.clear()
        viewBinding.newPassEtvp.text?.clear()
        viewBinding.confirmNewPassEtvp.text?.clear()
        viewBinding.oldPassEtvp.error = null
        viewBinding.newPassEtvp.error = null
        viewBinding.confirmNewPassEtvp.error = null
    }

    private fun setupNavBar(){
        viewBinding.calendarBtnLl.setOnClickListener {
            val intent = Intent(this, CalendarActivity::class.java)
            startActivity(intent)
        }
        viewBinding.profileBtnLl.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
        viewBinding.homeBtnLl.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
        viewBinding.savedBtnLl.setOnClickListener {
            val intent = Intent(this, SavedRecipeActivity::class.java)
            startActivity(intent)
        }
    }

    private fun editLoginState(userName: String? = null, userEmail: String? = null) {
        val editor = sp.edit()
        userName?.let { editor.putString("userName", it) }
        userEmail?.let { editor.putString("userEmail", it) }
        editor.apply()

        // Update local variables
        this.userName = userName ?: this.userName
        this.userEmail = userEmail ?: this.userEmail
    }
}