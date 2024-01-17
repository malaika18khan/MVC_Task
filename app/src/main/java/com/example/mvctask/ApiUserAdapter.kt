package com.example.mvctask

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ApiUserAdapter(
    var userList: List<ApiUser>,
    private val onItemClickListener: (ApiUser) -> Unit,
    private val onDeleteClickListener: (ApiUser) -> Unit,
    private val onEditClickListener: (ApiUser) -> Unit
) : RecyclerView.Adapter<ApiUserAdapter.ApiUserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApiUserViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.user_item_layout, parent, false)
        return ApiUserViewHolder(view)
    }

    override fun onBindViewHolder(holder: ApiUserViewHolder, position: Int) {
        val user = userList[position]

        holder.bind(user)

        holder.itemView.setOnClickListener { onItemClickListener(user) }
        holder.btnDelete.setOnClickListener { onDeleteClickListener(user) }
        holder.btnEdit.setOnClickListener { onEditClickListener(user) }
    }

    fun setApiUsers(users: List<ApiUser>) {
        userList = users
        notifyDataSetChanged()
    }

    fun removeApiUser(apiUser: ApiUser) {
        val updatedList = userList.toMutableList()
        updatedList.remove(apiUser)
        userList = updatedList
        notifyDataSetChanged()
    }

    fun updateApiUser(apiUser: ApiUser) {
        val updatedList = userList.toMutableList()
        val index = updatedList.indexOfFirst { it.id == apiUser.id }

        if (index != -1) {
            updatedList[index] = apiUser
            userList = updatedList
            notifyItemChanged(index)
        }
    }

    // Function to add a new user to the adapter
    fun addApiUser(apiUser: ApiUser) {
        val updatedList = userList.toMutableList()
        updatedList.add(apiUser)
        userList = updatedList
        notifyItemInserted(userList.size - 1)
    }


    override fun getItemCount(): Int {
        return userList.size
    }

    inner class ApiUserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtName: TextView = itemView.findViewById(R.id.textViewName)
        private val txtEmail: TextView = itemView.findViewById(R.id.textViewEmail)
        val btnDelete: TextView = itemView.findViewById(R.id.buttonDelete)
        val btnEdit: TextView = itemView.findViewById(R.id.buttonEdit)

        fun bind(user: ApiUser) {
            txtName.text = user.name
            txtEmail.text = user.email
        }
    }
}
