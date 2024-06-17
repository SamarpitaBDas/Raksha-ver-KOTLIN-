package com.example.raksha

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

private const val ARG_USERNAME = "username"

class HomeFragment : Fragment() {

    private lateinit var emergencyCallsRef: DatabaseReference
    private lateinit var reportsRef: DatabaseReference
    private lateinit var casesRef: DatabaseReference

    private lateinit var emergencyCallsCountTextView: TextView
    private lateinit var reportsCountTextView: TextView
    private lateinit var casesCountTextView: TextView

    private var homeusername: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase Database references
        emergencyCallsRef = FirebaseDatabase.getInstance().getReference("emergency_calls")
        reportsRef = FirebaseDatabase.getInstance().getReference("reports")
        casesRef = FirebaseDatabase.getInstance().getReference("cases")

        // Retrieve username from arguments
        arguments?.let {
            homeusername = it.getString(ARG_USERNAME)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize TextViews
        emergencyCallsCountTextView = view.findViewById(R.id.emergencyCallsCountTextView)
        reportsCountTextView = view.findViewById(R.id.reportsCountTextView)
        casesCountTextView = view.findViewById(R.id.casesCountTextView)

        // Retrieve counts from Firebase
        retrieveEmergencyCallsCount()
        retrieveReportsCount()
        retrieveCasesCount()

        return view
    }

    private fun retrieveEmergencyCallsCount() {
        emergencyCallsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get total number of emergency calls
                val emergencyCallsCount: Long = dataSnapshot.childrenCount

                // Update TextView with the count
                emergencyCallsCountTextView.text = emergencyCallsCount.toString()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
                Toast.makeText(requireContext(), "Failed to retrieve emergency calls count", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun retrieveReportsCount() {
        reportsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get total number of reports
                val reportsCount: Long = dataSnapshot.childrenCount

                // Update TextView with the count
                reportsCountTextView.text = reportsCount.toString()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
                Toast.makeText(requireContext(), "Failed to retrieve reports count", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun retrieveCasesCount() {
        casesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get total number of cases
                val casesCount: Long = dataSnapshot.childrenCount

                // Update TextView with the count
                casesCountTextView.text = casesCount.toString()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
                Toast.makeText(requireContext(), "Failed to retrieve cases count", Toast.LENGTH_SHORT).show()
            }
        })
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param username Parameter 1.
         * @return A new instance of fragment HomeFragment.
         */
        @JvmStatic
        fun newInstance(username: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_USERNAME, username)
                }
            }
    }
}
