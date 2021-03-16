package com.ekoapp.sdk.comment.loader

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ekoapp.ekosdk.comment.EkoComment
import com.ekoapp.ekosdk.comment.EkoCommentReference
import com.ekoapp.sdk.R
import com.google.common.base.Objects
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.item_comment.view.*

class CommentAdapter(val commentList: MutableList<EkoComment>) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    private val onLongClickSubject = PublishSubject.create<EkoComment>()
    private val onClickSubject = PublishSubject.create<EkoComment>()

    val onLongClickFlowable: Flowable<EkoComment>
        get() = onLongClickSubject.toFlowable(BackpressureStrategy.BUFFER)

    val onClickFlowable: Flowable<EkoComment>
        get() = onClickSubject.toFlowable(BackpressureStrategy.BUFFER)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun getItemCount(): Int {
        return commentList.size
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = commentList[position]
        if (comment == null) {
            holder.itemView.comment_id_textview.text = "loading..."
        } else {
            if (comment.getState() == EkoComment.State.FAILED) {
                holder.itemView.comment_id_textview.visibility = View.GONE
            } else {
                holder.itemView.comment_id_textview.visibility = View.VISIBLE
                comment.let { commentNonNull ->
                    val data = commentNonNull.getData() as EkoComment.Data.TEXT
                    val text = StringBuilder()
                            .append("id: ")
                            .append(commentNonNull.getCommentId())
                            .append("\nreferenceType: ")
                            .append(getReferenceType(commentNonNull.getReference()))
                            .append("\ndata comment: ")
                            .append(data.getText())
                            .append("\nuser id: ")
                            .append(commentNonNull.getUser()?.getUserId() ?: "")
                            .append("\nparent id: ")
                            .append(commentNonNull.getParentId())
                            .append("\ndata type: ")
                            .append(commentNonNull.getDataType().apiKey)
                            .append("\nchildren number: ")
                            .append(commentNonNull.getChildrenNumber())
                            .append("\nreactions: ")
                            .append(commentNonNull.getReactionMap().toString())
                            .append("\nmy reactions: ")
                            .append(commentNonNull.getMyReactions().toString())
                            .append("\ndeleted: ")
                            .append(commentNonNull.isDeleted())
                            .append("\ncreated at: ")
                            .append(commentNonNull.getCreatedAt())
                            .toString()
                    holder.itemView.comment_id_textview.text = text

                    holder.itemView.setOnClickListener {
                        onClickSubject.onNext(commentNonNull)
                    }

                    holder.itemView.setOnLongClickListener {
                        onLongClickSubject.onNext(commentNonNull)
                        true
                    }
                }
            }
        }
    }

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private fun getReferenceType(reference: EkoCommentReference): String {
        return when (reference) {
            is EkoCommentReference.Content -> {
                "Content"
            }
            is EkoCommentReference.Post -> {
                "Post"
            }
            else -> {
                "Unknown"
            }
        }
    }


    class CommentDiffCallback(val oldList: List<EkoComment>, val newList: List<EkoComment>) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldComment = oldList[oldItemPosition]
            val newComment = newList[newItemPosition]
            return oldComment.getCommentId() == newComment.getCommentId()
        }

        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            return (Objects.equal(oldItem.getCommentId(), newItem.getCommentId())
                    && Objects.equal(oldItem.getReference(), newItem.getReference())
                    && Objects.equal(oldItem.getParentId(), newItem.getParentId())
                    && Objects.equal(oldItem.getDataType(), newItem.getDataType())
                    && Objects.equal(oldItem.getChildrenNumber(), newItem.getChildrenNumber())
                    && Objects.equal(oldItem.getData(), newItem.getData())
                    && Objects.equal(oldItem.getFlagCount(), newItem.getFlagCount())
                    && Objects.equal(oldItem.getReactionMap(), newItem.getReactionMap())
                    && Objects.equal(oldItem.getReactionCount(), newItem.getReactionCount())
                    && Objects.equal(oldItem.getUser(), newItem.getUser())
                    && Objects.equal(oldItem.getState(), newItem.getState())
                    && Objects.equal(oldItem.isEdited(), newItem.isEdited()
                    && Objects.equal(oldItem.isDeleted(), newItem.isDeleted())))
        }

    }


    fun updateListItems(comments: List<EkoComment>) {
        val diffCallback = CommentDiffCallback(this.commentList, comments)
        val diffResult: DiffUtil.DiffResult = DiffUtil.calculateDiff(diffCallback)
        this.commentList.clear()
        this.commentList.addAll(comments)
        diffResult.dispatchUpdatesTo(this)
    }


}