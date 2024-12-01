package com.example.quantex.fragment

import android.app.ProgressDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.quantex.databinding.FragmentAccountBinding
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class FragAccount : Fragment() {

    private lateinit var binding: FragmentAccountBinding
    private lateinit var swipeToRefresh: SwipeRefreshLayout
    private lateinit var shimmerFrameLayout: ShimmerFrameLayout
    private var userId: String = ""
    private val username = arrayOf("")
    private val useremail = arrayOf("")
    private val userfunds = arrayOf("")
    private val totaltransactions = arrayOf("")
    private val positivetransactions = arrayOf("")
    private val negativetransactions = arrayOf("")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAccountBinding.inflate(inflater, container, false)

        // find shimmer.
        shimmerFrameLayout = binding.accountShimmer

        swipeToRefresh = binding.swipeRefreshLayout

        // get current user ID
        val currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        userId = currentUser?.uid ?: ""     // get user UID.

        // reload Account data.
        swipeToRefresh.setOnRefreshListener {
            binding.body.visibility = View.INVISIBLE
            loadAccountData()
            swipeToRefresh.isRefreshing = false
        }

        binding.body.visibility = View.INVISIBLE
        loadAccountData()

        return binding.root
    }


    private fun loadAccountData() {

            shimmerFrameLayout.startShimmer()
            shimmerFrameLayout.visibility = View.VISIBLE

            // get user data from realtime database
            FirebaseDatabase.getInstance().getReference("Users").child(userId).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var i = 0
                    for (snapshot in dataSnapshot.children) {
                        when (i) {
                            3 -> useremail[0] = snapshot.value.toString()
                            5 -> username[0] = snapshot.value.toString()
                        }
                        i++
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), error.message , Toast.LENGTH_SHORT).show()
                }
            })

            // User funds
            FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Funds").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var i = 0
                    for (snapshot in dataSnapshot.children) {
                        if (i == 0) {
                            userfunds[0] = snapshot.value.toString()
                        }
                        i++
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), error.message , Toast.LENGTH_SHORT).show()
                }
            })

            // User transactions
            FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Transactions").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var i = 0
                    for (snapshot in dataSnapshot.children) {
                        when (i) {
                            0 -> negativetransactions[0] = snapshot.value.toString()
                            1 -> positivetransactions[0] = snapshot.value.toString()
                            2 -> totaltransactions[0] = snapshot.value.toString()
                        }
                        i++
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), error.message , Toast.LENGTH_SHORT).show()
                }
            })

            Handler(Looper.getMainLooper()).postDelayed({
                shimmerFrameLayout.stopShimmer()
                shimmerFrameLayout.visibility = View.GONE
                binding.body.visibility = View.VISIBLE
                binding.username.text = "${username[0]}'s Account Summary"
                binding.mail.text = "E-mail: ${useremail[0]}"
                binding.funds.text = "$${userfunds[0]}"
                binding.totaltrans.text = totaltransactions[0]
                binding.positivetrans.text = positivetransactions[0]
                binding.negativetrans.text = negativetransactions[0]
            }, 3000)


    }


}
