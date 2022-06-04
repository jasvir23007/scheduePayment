package com.okaythis.jasvir.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.okaythis.jasvir.databinding.ItemTransactionViewBinding

class PaymentAdapter(private val paymentList: List<String>) : RecyclerView.Adapter<PaymentAdapter.PaymentHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentHolder {
        val itemBinding = ItemTransactionViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PaymentHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: PaymentHolder, position: Int) {
        val paymentBean = paymentList[position]
        holder.bind(paymentBean)
    }

    override fun getItemCount(): Int = paymentList.size

    class PaymentHolder(private val itemBinding: ItemTransactionViewBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(paymentBean: String) {
            itemBinding.tvTransaction.text = paymentBean

        }
    }
}