package com.example.yourway.forum

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.yourway.R

class CommentAdapter(private val commentList: List<Comment>) :
    RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val content: TextView = itemView.findViewById(R.id.tv_comment_content)
        val upvoteCount: TextView = itemView.findViewById(R.id.tv_comment_upvote_count)
        val replyButton: Button = itemView.findViewById(R.id.tv_comment_reply_button)
        val replyLayout: LinearLayout = itemView.findViewById(R.id.tv_comment_reply_layout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.forum_comment_item, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = commentList[position]
        holder.content.text = comment.content
        holder.upvoteCount.text = comment.upvotes.toString()

        // Handle reply functionality
        holder.replyButton.setOnClickListener {
            holder.replyLayout.visibility = if (holder.replyLayout.visibility == View.GONE) View.VISIBLE else View.GONE
        }
    }

    override fun getItemCount() = commentList.size
}
