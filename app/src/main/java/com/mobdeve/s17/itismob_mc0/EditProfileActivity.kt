package com.mobdeve.s17.itismob_mc0

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.mobdeve.s17.itismob_mc0.databinding.EditProfilePageBinding

class EditProfileActivity : ComponentActivity() {
    private lateinit var viewBinding: EditProfilePageBinding
    private val originalName = "Adrian"
    private val originalEmail = "sample@gmail.com"
    private val originalPassword = "1234567"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = EditProfilePageBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        setupNavBar()
        // Set current values in EditText fields
        viewBinding.nameEtv.setText(originalName)
        viewBinding.emailEtv.setText(originalEmail)

        viewBinding.epSaveChangesBtn.setOnClickListener {
            // Get values INSIDE the click listener when button is clicked
            val name = viewBinding.nameEtv.text.toString().trim()
            val email = viewBinding.emailEtv.text.toString().trim()
            val oldPass = viewBinding.oldPassEtvp.text.toString().trim()
            val newPass = viewBinding.newPassEtvp.text.toString().trim()
            val confirmNewPass = viewBinding.confirmNewPassEtvp.text.toString().trim()

            Log.d("DEBUG", "Name: $name, Email: $email")
            Log.d("DEBUG", "OldPass: $oldPass, NewPass: $newPass, ConfirmPass: $confirmNewPass")

            val nameChanged = name != originalName
            val emailChanged = email != originalEmail
            val passwordFieldsFilled = oldPass.isNotEmpty() || newPass.isNotEmpty() || confirmNewPass.isNotEmpty()

            Log.d("DEBUG", "Name changed: $nameChanged, Email changed: $emailChanged, Password fields filled: $passwordFieldsFilled")

            when {
                // No changes at all
                !nameChanged && !emailChanged && !passwordFieldsFilled -> {
                    Toast.makeText(this, "No changes made", Toast.LENGTH_SHORT).show()
                }

                // Only password change attempted
                passwordFieldsFilled && !nameChanged && !emailChanged -> {
                    if (validatePasswordChange(oldPass, newPass, confirmNewPass)) {
                        saveAllChanges(name, email, newPass)
                        Toast.makeText(this, "Changes Saved", Toast.LENGTH_SHORT).show()
                    }
                }

                // Only name/email changes
                (nameChanged || emailChanged) && !passwordFieldsFilled -> {
                    saveAllChanges(name, email, null)
                    Toast.makeText(this, "Changes Saved", Toast.LENGTH_SHORT).show()
                }

                // Both name/email and password changes
                else -> {
                    if (validatePasswordChange(oldPass, newPass, confirmNewPass)) {
                        saveAllChanges(name, email, newPass)
                        Toast.makeText(this, "Changes Saved", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun validatePasswordChange(oldPass: String, newPass: String, confirmNewPass: String): Boolean {
        return when {
            oldPass.isEmpty() || newPass.isEmpty() || confirmNewPass.isEmpty() -> {
                Toast.makeText(this, "Please fill all password fields", Toast.LENGTH_SHORT).show()
                false
            }

            oldPass != originalPassword -> { // Replace with actual password check
                Toast.makeText(this, "Old password is incorrect", Toast.LENGTH_SHORT).show()
                false
            }

            newPass != confirmNewPass -> {
                Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show()
                false
            }

            newPass.length < 8 -> {
                Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT)
                    .show()
                false
            }

            else -> true
        }

    }

    private fun saveAllChanges(name: String, email: String, newPassword: String?) {
        // Save name and email
        var newname: String = ""
        var newemail: String = ""

        if (name.isNotEmpty())
            newname = "Adrian"


        if (email.isNotEmpty())
            newemail = "sample@gmail.com"

        Log.d("DEBUG", "New Name: $newname")
        Log.d("DEBUG", "New Email: $newemail")

        //newPassword?.let { savePassword(it) }
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
//      viewBinding.savedBtnLl.setOnClickListener {
//          val intent = Intent(this, SavedActivity::class.java)
//          startActivity(intent)
//      }
        viewBinding.homeBtnLl.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
    }

}