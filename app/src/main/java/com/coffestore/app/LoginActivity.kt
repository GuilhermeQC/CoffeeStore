package com.coffestore.app;

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.coffestore.app.R
import com.coffestore.app.data.AppDatabase
import com.coffestore.app.data.User
import com.coffestore.app.data.UserDao
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var userDao: UserDao

    override fun onCreate(savedInstanceState: Bundle?) {

        //Esconde UI
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.login)
        hideSystemBars()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        val database = AppDatabase.getDatabase(this)
        userDao = database.userDao()

        insertDefaultUser()

        val editTextUsuario = findViewById<EditText>(R.id.usernameEditText)
        val editTextSenha = findViewById<EditText>(R.id.passwordEditText)
        val buttonEntrar = findViewById<Button>(R.id.loginButton)

        buttonEntrar.setOnClickListener {
            val usuario = editTextUsuario.text.toString()
            val senha = editTextSenha.text.toString()

            if (usuario.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show()
            } else {

                lifecycleScope.launch {
                    val user = userDao.findUser(usuario, senha)


                    if (user != null) {

                        runOnUiThread {
                            Toast.makeText(this@LoginActivity, "Login bem-sucedido!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@LoginActivity, PedidosActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    } else {

                        runOnUiThread {
                            Toast.makeText(this@LoginActivity, "Usuário ou senha inválidos", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private fun insertDefaultUser() {
        lifecycleScope.launch {
            val adminUser = userDao.findUser("admin", "1234")
            if (adminUser == null) {
                userDao.insert(User(username = "admin", password = "1234"))
            }
        }
    }

    private fun hideSystemBars() {
        val windowInsetsController =
            WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

}