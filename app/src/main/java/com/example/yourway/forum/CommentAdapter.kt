package com.example.yourway.forum

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.yourway.R
class CommentAdapter(
    private val commentList: List<Comment>,
    private val onReplySend: (String, String) -> Unit, // Callback for sending replies
    private val onLikeComment: (String) -> Unit // Callback for liking the comment
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val content: TextView = itemView.findViewById(R.id.tv_comment_content)

        val likeCount: TextView = itemView.findViewById(R.id.tv_comment_like_count)
        val replyButton: Button = itemView.findViewById(R.id.tv_comment_reply_button)
        val likeButton: ImageButton = itemView.findViewById(R.id.btn_comment_like)
        val replyLayout: LinearLayout = itemView.findViewById(R.id.tv_comment_reply_layout)
        val replyInput: EditText = itemView.findViewById(R.id.et_reply_input)
        val sendReplyButton: Button = itemView.findViewById(R.id.btn_send_reply)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.forum_comment_item, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = commentList[position]
        holder.content.text = comment.content
        holder.likeCount.text = comment.likes.toString() // Display the number of likes

        // Toggle reply layout visibility
        holder.replyButton.setOnClickListener {
            holder.replyLayout.visibility = if (holder.replyLayout.visibility == View.GONE) View.VISIBLE else View.GONE
        }

        // Handle sending the reply
        holder.sendReplyButton.setOnClickListener {
            val replyText = holder.replyInput.text.toString().trim()
            if (replyText.isNotEmpty()) {
                onReplySend(replyText, comment.id) // Send the reply

                // Clear input and hide the reply layout after sending
                holder.replyInput.text.clear()
                holder.replyLayout.visibility = View.GONE
            }
        }

        // Handle like button click
        holder.likeButton.setOnClickListener {
            onLikeComment(comment.id) // Trigger the like event
        }
    }

    override fun getItemCount() = commentList.size
}

