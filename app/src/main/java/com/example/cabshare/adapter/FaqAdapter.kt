package com.example.cabshare.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cabshare.databinding.ItemFaqBinding
import com.example.cabshare.model.FaqItem

class FaqAdapter(private val faqs: List<FaqItem>) : RecyclerView.Adapter<FaqAdapter.FaqViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaqViewHolder {
        val binding = ItemFaqBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FaqViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FaqViewHolder, position: Int) {
        val faq = faqs[position]
        holder.binding.tvQuestion.text = faq.question
        holder.binding.tvAnswer.text = faq.answer
        holder.binding.tvAnswer.visibility = if (faq.isExpanded) View.VISIBLE else View.GONE
        holder.binding.ivExpand.rotation = if (faq.isExpanded) 180f else 0f

        holder.binding.llQuestion.setOnClickListener {
            faq.isExpanded = !faq.isExpanded
            notifyItemChanged(position)
        }
    }

    override fun getItemCount() = faqs.size

    class FaqViewHolder(val binding: ItemFaqBinding) : RecyclerView.ViewHolder(binding.root)
}
