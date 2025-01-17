package com.example.to_do_list_mobile

import TaskRepository
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import java.lang.Exception
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ToDoListAdapter(
    private val toDoList: ArrayList<Task>,
    private var taskRepository: TaskRepository,
    private val deleteCallback: (Int) -> Unit
) : RecyclerView.Adapter<ToDoListAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val task: LinearLayout = view.findViewById(R.id.task)
        val taskDateView: TextView = view.findViewById(R.id.date_view)
        val taskDateEdit: EditText = view.findViewById(R.id.date_edit)
        val taskDescriptionView: TextView = view.findViewById(R.id.description_view)
        val taskDescriptionEdit: EditText = view.findViewById(R.id.description_edit)
        var completedSwitch: Switch = view.findViewById(R.id.completedSwitch)
        val deleteButton: ImageButton = view.findViewById(R.id.deleteBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_todo, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val taskId = toDoList[position].id;
        taskRepository.getTaskFromDatabase(taskId) { task ->
            if (task != null){
                holder.taskDescriptionView.text = task.description
                if (task.description !== "description") {
                    holder.taskDescriptionEdit.setText(task.description)
                }

                holder.taskDescriptionView.setOnClickListener {
                    holder.taskDescriptionView.visibility = View.GONE
                    holder.taskDescriptionEdit.visibility = View.VISIBLE
                    holder.taskDescriptionEdit.requestFocus()

                    val imm = holder.taskDescriptionEdit.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(holder.taskDescriptionEdit, InputMethodManager.SHOW_IMPLICIT)
                }

                holder.taskDescriptionEdit.setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_UP) {
                        holder.taskDescriptionEdit.requestFocus()
                    }
                    false
                }

                holder.taskDescriptionEdit.setOnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) {
                        val newDescription = holder.taskDescriptionEdit.text.toString()
                        taskRepository.changeDescription(taskId, newDescription)

                        val imm = holder.taskDescriptionEdit.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(holder.taskDescriptionEdit.windowToken, 0)

                        holder.taskDescriptionView.text = newDescription
                        holder.taskDescriptionView.visibility = View.VISIBLE
                        holder.taskDescriptionEdit.visibility = View.GONE
                    }
                }

                holder.taskDescriptionEdit.setOnClickListener {
                    if (holder.taskDescriptionEdit.hasFocus()) {
                        val newDescription = holder.taskDescriptionEdit.text.toString()
                        taskRepository.changeDescription(taskId, newDescription)

                        val imm = holder.taskDescriptionEdit.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(holder.taskDescriptionEdit.windowToken, 0)

                        holder.taskDescriptionView.text = newDescription
                        holder.taskDescriptionView.visibility = View.VISIBLE
                        holder.taskDescriptionEdit.visibility = View.GONE
                    }
                }

                holder.taskDateView.text = task.date
                holder.taskDateEdit.setText(task.date)

                holder.taskDateView.setOnClickListener {
                    holder.taskDateView.visibility = View.GONE
                    holder.taskDateEdit.visibility = View.VISIBLE
                    holder.taskDateEdit.requestFocus()

                    val imm = holder.taskDateEdit.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(holder.taskDateEdit, InputMethodManager.SHOW_IMPLICIT)
                }

                holder.taskDateEdit.setOnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) {
                        val newDate = holder.taskDateEdit.text.toString()

                        if (checkDate(newDate)) {
                            taskRepository.changeDate(taskId, newDate)
                            holder.taskDateView.text = newDate
                        } else {
                            holder.taskDateEdit.setText(task.date)
                            Toast.makeText(holder.itemView.context, "Дата введена некорректно!", Toast.LENGTH_SHORT).show()
                        }

                        val imm = holder.taskDateEdit.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(holder.taskDateEdit.windowToken, 0)

                        holder.taskDateView.visibility = View.VISIBLE
                        holder.taskDateEdit.visibility = View.GONE
                    }
                }

                holder.taskDateEdit.setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_UP) {
                        holder.taskDateEdit.requestFocus()
                    }
                    false
                }

                holder.taskDateEdit.setOnClickListener {
                    if (holder.taskDateEdit.hasFocus()) {
                        val newDate = holder.taskDateEdit.text.toString()

                        if (checkDate(newDate)) {
                            taskRepository.changeDate(taskId, newDate)
                            holder.taskDateView.text = newDate
                        } else {
                            holder.taskDateEdit.setText(task.date)
                            Toast.makeText(holder.itemView.context, "Дата введена некорректно!", Toast.LENGTH_SHORT).show()
                        }

                        val imm = holder.taskDateEdit.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(holder.taskDateEdit.windowToken, 0)

                        holder.taskDateView.visibility = View.VISIBLE
                        holder.taskDateEdit.visibility = View.GONE
                    }
                }


                holder.completedSwitch.setOnCheckedChangeListener(null)
                holder.completedSwitch.isChecked = task.isCompleted

                updateSwitch(
                    holder.completedSwitch,
                    holder.task,
                    task.isCompleted,
                    holder.taskDateView,
                    holder.taskDescriptionView,
                    holder.itemView.context
                )

                holder.completedSwitch.setOnCheckedChangeListener { _, isChecked ->
                    task.isCompleted = isChecked

                    taskRepository.switchCompleted(taskId)

                    updateSwitch(
                        holder.completedSwitch,
                        holder.task,
                        isChecked,
                        holder.taskDateView,
                        holder.taskDescriptionView,
                        holder.itemView.context
                    )
                }

                holder.deleteButton.setOnClickListener {
                    taskRepository.deleteOneTask(taskId);
                    deleteCallback(position)
                }
            }
        }
    }

    private fun updateSwitch (switch: Switch, task: LinearLayout, completed: Boolean, date: TextView, description: TextView, context: Context) {
        if (completed) {
            switch.thumbDrawable = ContextCompat.getDrawable(context, R.drawable.switch_thumb_on)
            switch.trackDrawable = ContextCompat.getDrawable(context, R.drawable.switch_track_on)
            task.background = ContextCompat.getDrawable(context, R.drawable.rounded_corner_light_green)
            date.paintFlags = date.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            description.paintFlags = description.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            switch.thumbDrawable = ContextCompat.getDrawable(context, R.drawable.switch_thumb)
            switch.trackDrawable = ContextCompat.getDrawable(context, R.drawable.switch_track)
            task.background = ContextCompat.getDrawable(context, R.drawable.rounded_corner_white)
            date.paintFlags = date.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            description.paintFlags = description.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkDate(date: String): Boolean {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return try {
            LocalDate.parse(date, formatter)
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun getItemCount(): Int {
        return toDoList.size
    }
}
