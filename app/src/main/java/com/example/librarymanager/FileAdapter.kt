package com.example.librarymanager

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import android.widget.Toast
import com.example.librarymanager.data.FileItem


class FileAdapter(
    private val context: Context,
    private val files: List<FileItem>,
    private val downloadFile: (FileItem) -> Unit
) : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    inner class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fileNameTextView: TextView = itemView.findViewById(R.id.fileNameTextView)
        val downloadButton: Button = itemView.findViewById(R.id.downloadButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_file, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val fileItem = files[position]
        holder.fileNameTextView.text = fileItem.fileName
        holder.downloadButton.setOnClickListener {
            downloadFile(fileItem)
        }
    }

    override fun getItemCount(): Int {
        return files.size
    }
}
