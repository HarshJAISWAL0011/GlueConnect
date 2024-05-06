package com.example.chatapplication.profile

import android.content.Context
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.Constants
import com.example.Constants.MY_ID
import com.example.chatapplication.R
import com.example.chatapplication.firebase.FirestoreDb.getProfileData
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterialApi::class,
    ExperimentalMaterial3Api::class
)
 @Composable
 fun DetailScreen(context: Context,cancel:()->Unit) {
    val desc = remember { mutableStateOf(TextFieldValue()) }
    val location = remember { mutableStateOf(TextFieldValue()) }
    var jobType by remember { mutableStateOf("") }
    var name by remember {mutableStateOf(TextFieldValue()) }
    val about = remember { mutableStateOf(TextFieldValue()) }
    var descError by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }
    var aboutError by remember { mutableStateOf(false) }
    var locationError by remember { mutableStateOf(false) }
    val skills = remember { mutableStateListOf<String>() }
    var defaultIdx by remember { mutableStateOf(0) }
    var showDialogState by remember { mutableStateOf(true) }

    LaunchedEffect (Unit){
        GlobalScope.launch {
            getProfileData(MY_ID).addOnCompleteListener{
                if(it.isSuccessful){
                    val map = it.result
                    if(map.containsKey("description")) desc.value = TextFieldValue(map["description"].toString())
                    if(map.contains("location")) location.value = TextFieldValue(map["location"].toString())
                    if(map.contains("about")) about.value = TextFieldValue(map["about"].toString())
                    if(map.contains("skills")) skills.addAll(map["skills"] as List<String>)
                    if(map.contains("name")) name = TextFieldValue(map["name"].toString())

                    var type = ""
                    if(map.contains("job_type")) type = map["job_type"].toString()

                     defaultIdx = when (type) {
                        "Android Developer" -> 1
                        "Frontend Developer" -> 2
                        "Backend Developer" -> 3
                        "Devops Engineer" -> 4
                        "Fullstack Developer" -> 5
                        "Other" -> 6
                        else -> 0
                    }
                    showDialogState =false
                    println("map got =${skills.toList()}")
                }
            }
        }
    }


    if (showDialogState) {
        AlertDialog(
            onDismissRequest = { },
            modifier = Modifier.background(Color.Transparent),
        )
        {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
            ) {
                CircularProgressIndicator(
                    color = colorResource(id = R.color.primary),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

        }
    }


    LazyColumn(modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp)) {
        item {

            Text(
                text = "Name", modifier = Modifier.padding(top = 35.dp, bottom = 1.dp),
                fontSize = 17.sp, fontFamily = FontFamily(Font(R.font.merriweather)),
                color = Color.Black.copy(0.8f)
            )

            TextField(
                value = name,
                onValueChange = { name = it
                    descError = false},
                maxLines = 3,
                isError = nameError,
                placeholder = { Text(text = "Name", color = Color.Gray.copy(0.5f)) },
                colors = TextFieldDefaults.textFieldColors(
                    textColor = Color.Black.copy(0.8f),
                    backgroundColor = Color.Transparent,
                    cursorColor = colorResource(id = R.color.primary),
                    focusedIndicatorColor = colorResource(id = R.color.primary),
                    unfocusedIndicatorColor = colorResource(id = R.color.primary).copy(0.7f),
                    errorIndicatorColor = Color.Red

                ),
                textStyle = TextStyle(fontSize = 17.sp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 20.dp)
                    .defaultMinSize(minHeight = 1.dp)

            )


            Text(
                text = "Description", modifier = Modifier.padding(top = 35.dp, bottom = 1.dp),
                fontSize = 17.sp, fontFamily = FontFamily(Font(R.font.merriweather)),
                color = Color.Black.copy(0.8f)
            )

            TextField(
                value = desc.value,
                onValueChange = { desc.value = it
                                descError = false},
                maxLines = 3,
                isError = descError,
                placeholder = { Text(text = "Description", color = Color.Gray.copy(0.5f)) },
                colors = TextFieldDefaults.textFieldColors(
                    textColor = Color.Black.copy(0.8f),
                    backgroundColor = Color.Transparent,
                    cursorColor = colorResource(id = R.color.primary),
                    focusedIndicatorColor = colorResource(id = R.color.primary),
                    unfocusedIndicatorColor = colorResource(id = R.color.primary).copy(0.7f),
                    errorIndicatorColor = Color.Red

                ),
                textStyle = TextStyle(fontSize = 17.sp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 20.dp)
                    .defaultMinSize(minHeight = 1.dp)

            )

            Text(
                text = "About", modifier = Modifier.padding(top = 35.dp, bottom = 20.dp),
                fontSize = 17.sp, fontFamily = FontFamily(Font(R.font.merriweather)),
               color = Color.Black.copy(0.8f)
            )

            OutlinedTextField(
                value = about.value,
                onValueChange = { about.value = it
                                aboutError = false},
                maxLines = 7,
                isError = aboutError,
                placeholder = { Text(text = "About", color = Color.Gray.copy(0.5f)) },
                colors = TextFieldDefaults.textFieldColors(
                    textColor = Color.Black.copy(0.8f),
                    backgroundColor = Color.Transparent,
                    cursorColor = colorResource(id = R.color.primary),
                    focusedIndicatorColor = colorResource(id = R.color.primary),
                    unfocusedIndicatorColor = colorResource(id = R.color.primary).copy(0.7f),
                    errorIndicatorColor = Color.Red

                ),
                textStyle = TextStyle(fontSize = 17.sp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 20.dp)
                    .defaultMinSize(minHeight = 100.dp)

            )


            Text(
                text = "Location", modifier = Modifier.padding(top = 35.dp, bottom = 1.dp),
                fontSize = 17.sp, fontFamily = FontFamily(Font(R.font.merriweather)),
                color = Color.Black.copy(0.8f)
            )

            TextField(
                value = location.value,
                onValueChange = { location.value = it
                                  locationError = false},
                maxLines = 1,
                isError = locationError,
                placeholder = { Text(text = "Bangalore, India", color = Color.Gray.copy(0.5f)) },
                colors = TextFieldDefaults.textFieldColors(
                    textColor = Color.Black.copy(0.8f),
                    backgroundColor = Color.Transparent,
                    cursorColor = colorResource(id = R.color.primary),
                    focusedIndicatorColor = colorResource(id = R.color.primary),
                    unfocusedIndicatorColor = colorResource(id = R.color.primary).copy(0.7f),
                    errorIndicatorColor = Color.Red

                ),
                textStyle = TextStyle(fontSize = 17.sp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 20.dp)
                    .defaultMinSize(minHeight = 1.dp)

            )


            SkillFilterChips(skills.toList(),{
                skills.add(it)
            }){
                skills.remove(it)
        }
            JobRole(defaultIdx){
                jobType= it
            }

            // Action Buttons
            ActionButtons({
               // Update clicked
                if(name.text.isEmpty()){
                    nameError = true
                    return@ActionButtons
                }

               if(desc.value.text.isEmpty()){
                   descError = true
                   return@ActionButtons
                }

                if(about.value.text.isEmpty()){
                    aboutError = true
                    return@ActionButtons
                }

                if(location.value.text.isEmpty()){
                    locationError = true
                    return@ActionButtons
                }

                if(jobType.isEmpty()){
                    Toast.makeText(context, "Select Job role", Toast.LENGTH_LONG).show()
                    return@ActionButtons
                }

                if(skills.isEmpty()){
                    Toast.makeText(context, "Add at least one skill", Toast.LENGTH_LONG).show()
                    return@ActionButtons
                }
                showDialogState  = true
                var datamap = mapOf(
                    "name" to name.text,
                    "description" to desc.value.text,
                    "about" to about.value.text,
                    "location" to location.value.text,
                    "skills" to skills.toList(),
                    "job_type" to jobType
                )
                GlobalScope.launch {
                    val ref = Firebase.firestore.collection(Constants.path_users).document(MY_ID)
                    ref.set(datamap, SetOptions.merge()).addOnCompleteListener{
                        if(it.isSuccessful){
                            showDialogState = false
                            cancel()
                        }
                    }
                }

                println("data recorded = $datamap")

            },{
              // cancel clicked
                cancel()
            })


        }
    }
}


@Composable
private fun JobRole (defaultIdx: Int, selectedJob: (job: String)->Unit) {
    Column {
        var expanded by remember { mutableStateOf(false) }
        val options = listOf("Select role", "Android Developer", "Frontend Developer","Backend Developer","Devops Engineer", "Fullstack Developer","Other")
        var selectedIndex by remember { mutableStateOf(defaultIdx) }
        LaunchedEffect(defaultIdx) {
            selectedIndex=defaultIdx
            selectedJob(options[selectedIndex])
        }


        TextField(
            value = options[selectedIndex],
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = "",
                    tint = colorResource(
                        id = R.color.primary
                    )
                )
            },
            maxLines = 1,
            enabled = false,
            placeholder = { Text(text = "Select role") },
            colors = TextFieldDefaults.textFieldColors(
                textColor = Color.Black.copy(0.8f),
                backgroundColor = Color.Transparent,
                disabledTrailingIconColor = colorResource(id = R.color.primary),
                cursorColor = colorResource(id = R.color.primary),
                disabledTextColor = Color.Black.copy(0.8f),
                focusedIndicatorColor = colorResource(id = R.color.primary),
                disabledIndicatorColor = colorResource(id = R.color.primary).copy(0.7f),
            ),
            textStyle = TextStyle(fontSize = 17.sp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 20.dp, top = 25.dp)
                .defaultMinSize(minHeight = 1.dp)
                .clickable {
                    expanded = !expanded
                }


        )

        // Dropdown menu
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    onClick = {
                        selectedIndex = index
                        if(selectedIndex == 0) return@DropdownMenuItem
                        expanded = false
                        selectedJob(options[selectedIndex])

                    }
                ) {
                    Text(
                        text = option,
                        style = MaterialTheme.typography.body1,
                        color = if (index == selectedIndex) MaterialTheme.colors.primary else Color.Unspecified
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButtons(okClicked: ()->Unit,cancelClicked: ()->Unit) {
    Row(
        modifier = Modifier
            .padding(top = 50.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Button(
            onClick =  cancelClicked,
            modifier = Modifier.padding(horizontal = 10.dp),
            elevation = ButtonDefaults.elevation(defaultElevation = 0.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.Transparent
            )
        ) {
            Text(text = "Cancel", color = colorResource(id = R.color.primary))
        }

        Button(
            onClick = okClicked,
            modifier = Modifier.padding(horizontal = 10.dp),
            elevation = ButtonDefaults.elevation(defaultElevation = 0.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = colorResource(id = R.color.primary)
            )
        ) {
            Text(text = "Update", color = Color.White)
        }
    }
}
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SkillFilterChips(sillList: List<String>,addSkill: (skill:String)->Unit,removeSkill: (skill:String)->Unit) {
    var newSkill by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 31.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Skills", modifier = Modifier.padding( bottom = 18.dp),
            fontSize = 17.sp, fontFamily = FontFamily(Font(R.font.merriweather)),
            color = Color.Black.copy(0.8f)
        )
        // Row to input new skill and add button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = newSkill,
                onValueChange = {newSkill = it },
                maxLines = 1,
                placeholder = { Text(text = "Enter a skill") },
                colors = TextFieldDefaults.textFieldColors(
                    textColor = Color.Black.copy(0.8f),
                    backgroundColor = Color.Transparent,
                    cursorColor = colorResource(id = R.color.primary),
                    focusedIndicatorColor = colorResource(id = R.color.primary),
                    unfocusedIndicatorColor = colorResource(id = R.color.primary).copy(0.7f),
                    errorIndicatorColor = Color.Red

                ),
                textStyle = TextStyle(fontSize = 17.sp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(end = 20.dp)
                    .defaultMinSize(minHeight = 1.dp)

            )


            Box(
                modifier = Modifier

                    .background(colorResource(id = R.color.background))
                    .shadow(10.dp, CircleShape)
                    .clip(CircleShape)
                    .background(colorResource(id = R.color.primary))
                    .size(40.dp)
                    .clickable {
                        if (newSkill.isNotBlank() && !sillList.contains(newSkill)) {
                            addSkill(newSkill)
                            newSkill = ""
                        }
                    }

            ) {
               Icon(Icons.Default.Add, contentDescription ="", tint = Color.White, modifier = Modifier.align(
                   Alignment.Center) )
            }
        }

        // Chips to display selected skills
        FlowRow(

            modifier = Modifier.padding(top = 16.dp)
        ) {
            sillList.forEach { skill ->
                FilterChip(
                    text = skill,
                    onDeleteClick = { removeSkill(skill) }
                )
            }
        }
    }
}

@Composable
fun FilterChip(
    text: String,
    onDeleteClick: () -> Unit
) {
    Card(
         elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .padding(horizontal = 5.dp)
            .clickable(onClick = onDeleteClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(id = R.color.primary),
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = text, color = Color.White, fontFamily = FontFamily(Font(R.font.robot_slab)))
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Delete",
                tint = Color.White,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(16.dp)
            )
        }
    }
}
