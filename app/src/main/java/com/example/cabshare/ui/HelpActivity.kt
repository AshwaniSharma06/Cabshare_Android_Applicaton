package com.example.cabshare.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cabshare.adapter.FaqAdapter
import com.example.cabshare.databinding.ActivityHelpBinding
import com.example.cabshare.model.FaqItem
import com.example.cabshare.viewmodel.HelpViewModel
import com.google.android.material.snackbar.Snackbar

class HelpActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHelpBinding
    private val viewModel: HelpViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHelpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupIssueTypeSpinner()
        setupFaqs()
        setupClickListeners()
        setupObservers()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupIssueTypeSpinner() {
        val issueTypes = arrayOf("App Bug", "Payment Issue", "Ride Issue", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, issueTypes)
        binding.actvIssueType.setAdapter(adapter)
    }

    private fun setupFaqs() {
        val faqs = listOf(
            FaqItem("How to book a ride?", "Search for your destination in 'Find Ride', select a trip that suits you, and click 'Book Ride'."),
            FaqItem("How to cancel a trip?", "Go to 'My Bookings', select the trip you want to cancel, and click 'Cancel Trip'."),
            FaqItem("How payment works?", "Payments are currently handled directly between users. We recommend discussing payment details in the chat."),
            FaqItem("Is my data safe?", "Yes, we use industry-standard encryption and Firebase's secure infrastructure to protect your data.")
        )
        binding.rvFaqs.layoutManager = LinearLayoutManager(this)
        binding.rvFaqs.adapter = FaqAdapter(faqs)
    }

    private fun setupClickListeners() {
        binding.cvEmailSupport.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf("support@cabshare.com"))
                putExtra(Intent.EXTRA_SUBJECT, "Support Request - CabShare App")
            }
            startActivity(Intent.createChooser(intent, "Send Email"))
        }

        binding.cvWhatsAppSupport.setOnClickListener {
            val url = "https://api.whatsapp.com/send?phone=+911234567890&text=Hello CabShare Support, I need help with..."
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }

        binding.btnSubmitTicket.setOnClickListener {
            val issueType = binding.actvIssueType.text.toString()
            val description = binding.etIssueDescription.text.toString()
            viewModel.submitTicket(issueType, description)
        }

        binding.btnMyTickets.setOnClickListener {
            // Optional: Start MyTicketsActivity if implemented
            Toast.makeText(this, "My Tickets feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSubmitTicket.isEnabled = !isLoading
        }

        viewModel.ticketSubmitted.observe(this) { submitted ->
            if (submitted) {
                binding.actvIssueType.setText("")
                binding.etIssueDescription.setText("")
                Snackbar.make(binding.root, "Ticket submitted successfully!", Snackbar.LENGTH_LONG).show()
                viewModel.clearStatus()
            }
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.clearStatus()
            }
        }
    }
}
