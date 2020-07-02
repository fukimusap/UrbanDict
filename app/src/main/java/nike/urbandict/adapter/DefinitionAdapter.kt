package nike.urbandict.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import nike.urbandict.R
import nike.urbandict.model.ProcessedDefinition

class DefinitionsAdapter : RecyclerView.Adapter<DefinitionViewHolder>() {

    private val differ: AsyncListDiffer<ProcessedDefinition> =
        AsyncListDiffer(this, object : DiffUtil.ItemCallback<ProcessedDefinition>() {
            override fun areItemsTheSame(
                oldItem: ProcessedDefinition,
                newItem: ProcessedDefinition
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: ProcessedDefinition,
                newItem: ProcessedDefinition
            ): Boolean {
                return oldItem.thumbsUp == newItem.thumbsUp && oldItem.thumbsDown == newItem.thumbsDown
                        && oldItem.definition == newItem.definition
            }
        })


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

}


