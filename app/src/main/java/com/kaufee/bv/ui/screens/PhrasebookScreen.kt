package com.kaufee.bv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaufee.bv.ui.theme.BV_Primary

data class PhraseItem(val category: String, val english: String, val hindi: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhrasebookScreen(
    onBack: () -> Unit,
    onTranslate: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val categories = listOf("All", "Travel", "Work", "Health")
    var selectedCategory by remember { mutableStateOf("All") }

    val allPhrases = listOf(
        PhraseItem("TRAVEL", "Where is the nearest metro station?", "नज़दीकी मेट्रो स्टेशन कहाँ है?"),
        PhraseItem("WORK", "Let's schedule a meeting for Monday.", "आइए सोमवार के लिए एक मीटिंग निर्धारित करें।"),
        PhraseItem("TRAVEL", "I would like to order a spicy curry.", "मैं एक तीखा करी मंगवाना चाहूंगा।"),
        PhraseItem("WORK", "Thank you for your cooperation.", "आपके सहयोग के लिए धन्यवाद।")
    )

    val filteredPhrases = allPhrases.filter {
        (selectedCategory == "All" || it.category == selectedCategory.uppercase()) &&
        (it.english.contains(searchQuery, ignoreCase = true) || it.hindi.contains(searchQuery, ignoreCase = true))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Phrasebook",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = BV_Primary
            )
            IconButton(onClick = { /* Settings */ }) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = Color.Gray)
            }
        }

        // Search Bar
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Search saved phrases...", fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            )
        )

        // Categories
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { Text(category) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF25379B),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFFF0F1FF),
                        labelColor = Color.Gray
                    ),
                    border = null,
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(filteredPhrases) { phrase ->
                PhraseCard(
                    category = phrase.category,
                    english = phrase.english,
                    hindi = phrase.hindi,
                    onClick = { onTranslate(phrase.english) }
                )
            }

            item {
                // Design banner in the middle
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF8E8FFA).copy(alpha = 0.6f), Color(0xFF4C4DDC).copy(alpha = 0.6f))
                            )
                        )
                ) {
                    Text(
                        "B",
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.White.copy(alpha = 0.2f),
                        fontSize = 100.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Global Communication", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Access your travel essentials even without an internet connection.", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            }

            item {
                // Did you know card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F1FF)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.HelpOutline, contentDescription = null, tint = BV_Primary)
                        Text("Did you know?", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            "Grouping phrases by categories helps you find them 40% faster when traveling.",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PhraseCard(category: String, english: String, hindi: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = if (category == "TRAVEL") Color(0xFFE0F7FA) else Color(0xFFE8EAF6)
            ) {
                Text(
                    text = category,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (category == "TRAVEL") Color(0xFF00ACC1) else Color(0xFF3F51B5)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "English", fontSize = 10.sp, color = Color.Gray)
            Text(text = english, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(text = "Hindi", fontSize = 10.sp, color = Color.Gray)
            Text(text = hindi, fontWeight = FontWeight.Bold, color = Color.Black)
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(Icons.AutoMirrored.Outlined.VolumeUp, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
            }
        }
    }
}
