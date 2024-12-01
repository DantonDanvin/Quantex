    package com.example.quantex

    import android.content.Intent
    import androidx.appcompat.app.AppCompatActivity
    import android.os.Bundle
    import android.os.Handler
    import android.os.Looper
    import android.view.Menu
    import android.view.MenuItem
    import android.view.WindowManager
    import android.widget.TextView
    import android.widget.Toast
    import androidx.appcompat.app.ActionBarDrawerToggle
    import androidx.appcompat.app.AlertDialog
    import androidx.appcompat.app.AppCompatDelegate
    import androidx.appcompat.widget.SwitchCompat
    import androidx.appcompat.widget.Toolbar
    import androidx.core.content.ContextCompat
    import androidx.core.view.GravityCompat
    import androidx.core.view.WindowCompat
    import androidx.drawerlayout.widget.DrawerLayout
    import androidx.fragment.app.Fragment
    import androidx.lifecycle.ViewModel
    import com.example.quantex.activitys.ContactUs
    import com.example.quantex.activitys.Feedback
    import com.example.quantex.activitys.Settings
    import com.example.quantex.authentication.SignIn
    import com.example.quantex.databinding.ActivityMainBinding
    import com.example.quantex.fragment.FragAccount
    import com.example.quantex.fragment.FragCrypto
    import com.example.quantex.fragment.FragMarket
    import com.example.quantex.fragment.FragPortfolio
    import com.example.quantex.fragment.FragTransactions
    import com.google.android.material.bottomnavigation.BottomNavigationView
    import com.google.android.material.navigation.NavigationView
    import com.google.firebase.auth.FirebaseAuth
    import com.google.firebase.database.DataSnapshot
    import com.google.firebase.database.DatabaseError
    import com.google.firebase.database.FirebaseDatabase
    import com.google.firebase.database.ValueEventListener

    class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, ExceptionListener {

        private lateinit var drawer: DrawerLayout
        private lateinit var binding: ActivityMainBinding
        private lateinit var bottomNavView: BottomNavigationView
        private lateinit var fragPortfolio: FragPortfolio

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setupExceptionHandler() // set Exception handler
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

        // Set system bar colors
        window.statusBarColor = ContextCompat.getColor(this, R.color.black_my)
        //window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        // Initialize drawer layout
        drawer = binding.drawer

        // Initialize the toolbar
        val toolbar: Toolbar = binding.toolbar
        setSupportActionBar(toolbar)

        // Initialize the NavigationView
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        // Setup the navigation view listener
        binding.navView.setNavigationItemSelectedListener(this)

        // open drawer nav
        val toggle = ActionBarDrawerToggle(this, drawer, binding.toolbar, R.string.open_nav, R.string.close_nav)
        drawer.addDrawerListener(toggle)
        toggle.syncState()


        // Bottom navigation listener.
        bottomNavView = findViewById(R.id.bottom_nav_view)
        bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_Market -> {
//                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, FragMarket()).commit()
                    switchFragment(FragMarket())
                    supportActionBar?.title = "Market" // Change the toolbar title
                    true
                }
                R.id.navigation_Portfolio -> {
                    switchFragment(FragPortfolio())
                    supportActionBar?.title = "Portfolio" // Change the toolbar title
                    true
                }
                R.id.navigation_crypto -> {
                    switchFragment(FragCrypto())
                    supportActionBar?.title = "Crypto Currency" // Change the toolbar title
                    true
                }
                R.id.navigation_transaction -> {
                    switchFragment(FragTransactions())
                    supportActionBar?.title = "Transactions" // Change the toolbar title
                    true
                }
                R.id.navigation_Account -> {
                    switchFragment(FragAccount())
                    supportActionBar?.title = "Account" // Change the toolbar title
                    true
                }
                else -> false
            }
        }
        // Bottom navigation item reselected listener (do nothing)
        bottomNavView.setOnItemReselectedListener { item ->
            // Do nothing when the item is reselected
        }



        // set Market fragment by default.
//        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, FragMarket()).commit()
        switchFragment(FragMarket())
        supportActionBar?.title = "Market" // Change the toolbar title
        bottomNavView.selectedItemId = R.id.navigation_Market


        // Get current user data
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid       // get user UID.
        val usermail = arrayOf("")
        val username = arrayOf("")

        if (userId != null) {
            FirebaseDatabase.getInstance().getReference("Users").child(userId)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        var i = 0
                        for (snapshot in dataSnapshot.children) {
                            when (i) {
                                3 -> usermail[0] = snapshot.value.toString()
                                5 -> username[0] = snapshot.value.toString()
                            }
                            i++
                        }
                        // Get access to the header XML file and find ID
                        val headerView = navigationView.getHeaderView(0)
                        val usernameHeader = headerView.findViewById<TextView>(R.id.username)
                        val usermailHeader = headerView.findViewById<TextView>(R.id.useremail)
                        usernameHeader.text = " ${username[0]}"
                        usermailHeader.text = " ${usermail[0]}"
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@MainActivity, error.message, Toast.LENGTH_SHORT).show()
                    }
                })
        }


    }   // End of on create function

    // on click drawer item change fragment
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.title) {
            "Portfolio" -> {
                drawer.closeDrawer(GravityCompat.START)
                supportActionBar?.title = "Portfolio" // Change the toolbar title
                bottomNavView.selectedItemId = R.id.navigation_Portfolio
                return true
            }
            "My Account" -> {
                drawer.closeDrawer(GravityCompat.START)
                supportActionBar?.title = "Account" // Change the toolbar title
                bottomNavView.selectedItemId = R.id.navigation_Account
                return true
            }
            "Transactions" -> {
                drawer.closeDrawer(GravityCompat.START)
                supportActionBar?.title = "Transactions" // Change the toolbar title
                bottomNavView.selectedItemId = R.id.navigation_transaction
                return true
            }
            "Crypto Currency" -> {
                drawer.closeDrawer(GravityCompat.START)
                supportActionBar?.title = "Crypto Currency" // Change the toolbar title
                bottomNavView.selectedItemId = R.id.navigation_crypto
                return true
            }
            //More.
            "Contact_us" -> {
                drawer.closeDrawer(GravityCompat.START)
                val intent = Intent(this, ContactUs::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                return true
            }
            "Share" -> {
                drawer.closeDrawer(GravityCompat.START)
                Toast.makeText(this@MainActivity,"Share App Link",Toast.LENGTH_SHORT).show()
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "App Link")
                }
                startActivity(intent)
                return true
            }
            // Other.
            "Settings" -> {
                drawer.closeDrawer(GravityCompat.START)
                val intent = Intent(this, Settings::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                return true
            }
            "Feedback" -> {
                drawer.closeDrawer(GravityCompat.START)
                val intent = Intent(this, Feedback::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                return true
            }
        }
        return true
    }

    private fun switchFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        // Get all fragments currently added to the FragmentManager
        val fragments = fragmentManager.fragments
        for (frag in fragments) {
            if (frag != null && frag.isVisible) {
                // Hide each fragment that is currently visible
                fragmentTransaction.hide(frag)
            }
        }

        // Check if the fragment to be added already exists in the fragment manager
        val existingFragment = fragmentManager.findFragmentByTag(fragment.javaClass.name)
        if (existingFragment != null) {
            if (existingFragment.isHidden) {
                // Show the existing fragment
                fragmentTransaction.show(existingFragment)
            }
        } else {
            // Add the new fragment and give it a tag
            fragmentTransaction.add(R.id.fragment_container, fragment, fragment.javaClass.name)
        }

        // Commit the transaction
        fragmentTransaction.commit()
    }




    // on press back if open then close drawer
    override fun onBackPressed() {
//        super.onBackPressed()
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            AlertDialog.Builder(this)
                .setTitle("Confirm Exit")
                .setMessage("Are you sure you want to close the app?")
                .setPositiveButton(android.R.string.yes) { dialog, which ->
                    // Close the app
                    finishAffinity()
                }
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
        }
    }

    // bellow code to get menu in main activity.
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    // bellow code for user click on menu item.
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.title) {
            "Logout" -> {
                FirebaseAuth.getInstance().signOut()
                Toast.makeText(this, "Logout Successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, SignIn::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // when Exception come handle here.
    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        Toast.makeText(this@MainActivity,"Exception occur",Toast.LENGTH_SHORT).show()
    }

    // set up Exception Handler.
    private fun setupExceptionHandler(){
        Handler(Looper.getMainLooper()).post {
            while (true) {
                try {
                    Looper.loop()
                } catch (e: Throwable) {
                    uncaughtException(Looper.getMainLooper().thread, e)
                }
            }
        }
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            uncaughtException(t, e)
        }
    }


}