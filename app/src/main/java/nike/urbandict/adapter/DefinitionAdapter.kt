package nike.urbandict.adapter

import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import nike.urbandict.R
import nike.urbandict.model.ProcessedDefinition

class DefinitionsAdapter : RecyclerView.Adapter<DefinitionViewHolder>() {

    private val differ: AsyncListDiffer<ProcessedDefinition> = AsyncListDiffer(this, object : DiffUtil.ItemCallback<ProcessedDefinition>() {
        override fun areItemsTheSame(oldItem: ProcessedDefinition, newItem: ProcessedDefinition): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ProcessedDefinition, newItem: ProcessedDefinition): Boolean {
            return oldItem.thumbsUp == newItem.thumbsUp && oldItem.thumbsDown == newItem.thumbsDown
                    && oldItem.definition == newItem.definition
        }
    })

    private var recyclerView: RecyclerView? = null

    init {
        differ.addListListener { _, currentList ->
            if (currentList.isNotEmpty()) {
                recyclerView?.let { view ->
                    view.post {
                        val linearSmoothScroller = object : LinearSmoothScroller(view.context) {
                            override fun getVerticalSnapPreference(): Int {
                                return SNAP_TO_START
                            }
                        }
                        linearSmoothScroller.targetPosition = 0
                        view.layoutManager?.startSmoothScroll(linearSmoothScroller)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun submitList(list: List<ProcessedDefinition>) {
        differ.submitList(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DefinitionViewHolder {
        return DefinitionViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_definition, parent, false)
        )
    }

    override fun onBindViewHolder(holder: DefinitionViewHolder, position: Int) {
        val item = differ.currentList[position]
        holder.titleView.text = item.word
        holder.definitionView.text = item.definition
        holder.thumbsUpView.text = item.thumbsUp.toString()
        holder.thumbsDownView.text = item.thumbsDown.toString()
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        if (this.recyclerView == recyclerView) {
            this.recyclerView = null
        }
    }
}

class DefinitionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val titleView: TextView = itemView.findViewById(R.id.titleView)
    val definitionView: TextView = itemView.findViewById(R.id.definitionView)
    val thumbsUpView: TextView = itemView.findViewById(R.id.thumbsUpView)
    val thumbsDownView: TextView = itemView.findViewById(R.id.thumbsDownView)

    init {
        definitionView.movementMethod = LinkMovementMethod.getInstance()
    }
}
