import re

with open('app/src/main/java/com/example/androidproject/MainActivity.kt', 'r') as f:
    content = f.read()

# 1. Remove background gradients
content = re.sub(r'val gradientColors = listOf\([\s\S]*?\n    \)', '', content)
content = re.sub(r'Box\(\s*modifier = Modifier\s*\.fillMaxSize\(\)\s*\.background\(Brush\.verticalGradient\(gradientColors\)\)\s*\)\s*\{', 'Box(modifier = Modifier.fillMaxSize()) {', content)

# 2. Update Scaffold
content = re.sub(r'containerColor = Color\.Transparent,', '', content)

# 3. Update TopAppBar
top_app_bar_old = """TopAppBar(
                    title = {
                        Text(
                            "University Management",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        IconButton(onClick = { showManagementModal = true }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Management",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White
                    )
                )"""

top_app_bar_new = """TopAppBar(
                    title = {
                        Text(
                            "University Management",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        IconButton(onClick = { showManagementModal = true }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Management"
                            )
                        }
                    }
                )"""
content = content.replace(top_app_bar_old, top_app_bar_new)

# 4. Remove all `color = Color.White` and similar inside Text and Icons for Phase 1
content = re.sub(r',\s*color = Color\.White', '', content)
content = re.sub(r',\s*color = Color\.White\.copy\(alpha = [0-9.]+[fF]\)', '', content)
content = re.sub(r',\s*color = Color\.Yellow', '', content)
# And tint
content = re.sub(r',\s*tint = Color\.White', '', content)
# And textStyle
content = re.sub(r',\s*textStyle = androidx\.compose\.ui\.text\.TextStyle\(color = Color\.White\)', '', content)

# 5. Remove ExposedDropdownMenuBox colors
content = re.sub(r'colors = ExposedDropdownMenuDefaults\.outlinedTextFieldColors\([\s\S]*?\),', '', content)
# Remove Modifier.background from ExposedDropdownMenu
content = re.sub(r'modifier = Modifier\.background\(Color\(0xFF2a2a3e\)\)', '', content)

# 6. Button colors
button_colors_regex = r'colors = ButtonDefaults\.buttonColors\(\s*containerColor = [^,]+,\s*contentColor = Color\.White\s*\)'
content = re.sub(button_colors_regex, '', content)
# other button colors
content = re.sub(r'colors = ButtonDefaults\.buttonColors\(containerColor = Color\.Transparent,\s*contentColor = Color\.White\)', '', content)
content = re.sub(r'colors = ButtonDefaults\.buttonColors\(containerColor = Color\.White\.copy\(alpha = 0\.2f\)\)', '', content)
content = re.sub(r'colors = ButtonDefaults\.buttonColors\(contentColor = Color\.White\)', '', content)

# Write back
with open('app/src/main/java/com/example/androidproject/MainActivity.kt', 'w') as f:
    f.write(content)

print("Phase 1 modifications applied")
