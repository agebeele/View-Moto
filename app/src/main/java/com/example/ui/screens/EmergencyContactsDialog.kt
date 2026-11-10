package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ShareLocation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EmergencyContact
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CockpitCardBg
import com.example.ui.theme.CockpitCardBorder
import com.example.ui.theme.HighContrastWhite
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.TextSecondary

@Composable
fun EmergencyContactsDialog(
    contacts: List<EmergencyContact>,
    onDismiss: () -> Unit,
    onAddContact: (name: String, phone: String, relationship: String, isPrimary: Boolean) -> Unit = { _, _, _, _ -> },
    onUpdateContact: (EmergencyContact) -> Unit = {},
    onDeleteContact: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var isAddingNew by remember { mutableStateOf(false) }
    var editingContact by remember { mutableStateOf<EmergencyContact?>(null) }

    var formName by remember { mutableStateOf("") }
    var formPhone by remember { mutableStateOf("") }
    var formRelation by remember { mutableStateOf("") }
    var formIsPrimary by remember { mutableStateOf(false) }

    fun dialPhoneNumber(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun sendSosLocationSms() {
        val primaryPhone = contacts.firstOrNull { it.isPrimary }?.phone ?: contacts.firstOrNull()?.phone ?: "911"
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$primaryPhone")
            putExtra(
                "sms_body",
                "¡ALERTA SOS MOTODASH! Requiero asistencia inmediata en moto. Coordenadas GPS: 19.4326,-99.1332. Ver mapa: https://maps.google.com/?q=19.4326,-99.1332"
            )
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Emergency,
                        contentDescription = "Emergencia",
                        tint = AlertRed,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CONTACTOS DE EMERGENCIA",
                        fontWeight = FontWeight.Black,
                        color = AlertRed,
                        fontSize = 16.sp
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("emergency_contacts_crud_dialog"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Call 911 Direct Action
                Button(
                    onClick = { dialPhoneNumber("911") },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("dial_911_button")
                ) {
                    Icon(imageVector = Icons.Default.LocalHospital, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("LLAMAR AL 911 (EMERGENCIAS)", fontWeight = FontWeight.Black, color = Color.White, fontSize = 12.sp)
                }

                // SOS Location SMS
                Button(
                    onClick = { sendSosLocationSms() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C1620)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .border(1.dp, AlertRed.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .testTag("send_sos_sms_button")
                ) {
                    Icon(imageVector = Icons.Default.ShareLocation, contentDescription = null, tint = AlertRed)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Enviar SMS de Alerta con Coordenadas GPS", fontWeight = FontWeight.Bold, color = AlertRed, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Contactos Registrados (${contacts.size})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = HighContrastWhite
                    )

                    TextButton(
                        onClick = {
                            formName = ""
                            formPhone = ""
                            formRelation = ""
                            formIsPrimary = false
                            editingContact = null
                            isAddingNew = true
                        },
                        modifier = Modifier.testTag("add_emergency_contact_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = SafeGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Agregar", color = SafeGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // List of Contacts with CRUD Actions
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    contacts.forEach { contact ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF161822)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    if (contact.isPrimary) AlertRed.copy(alpha = 0.8f) else CockpitCardBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .testTag("contact_item_${contact.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = contact.name,
                                            fontWeight = FontWeight.Bold,
                                            color = HighContrastWhite,
                                            fontSize = 13.sp
                                        )
                                        if (contact.isPrimary) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = AlertRed.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "PRINCIPAL",
                                                    color = AlertRed,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Black,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = "${contact.phone} · ${contact.relationship}",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Direct Dial Button
                                    IconButton(
                                        onClick = { dialPhoneNumber(contact.phone) },
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Call,
                                            contentDescription = "Llamar",
                                            tint = SafeGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    // Edit Button
                                    IconButton(
                                        onClick = {
                                            editingContact = contact
                                            formName = contact.name
                                            formPhone = contact.phone
                                            formRelation = contact.relationship
                                            formIsPrimary = contact.isPrimary
                                            isAddingNew = false
                                        },
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Editar contacto",
                                            tint = Color.LightGray,
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }

                                    // Delete Button
                                    IconButton(
                                        onClick = { onDeleteContact(contact.id) },
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Eliminar contacto",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = Color.Gray)
            }
        },
        containerColor = CockpitCardBg,
        modifier = modifier
    )

    // Sub-dialog: Add / Edit Contact Form
    if (isAddingNew || editingContact != null) {
        val isEditing = editingContact != null
        AlertDialog(
            onDismissRequest = {
                isAddingNew = false
                editingContact = null
            },
            title = {
                Text(
                    text = if (isEditing) "Editar Contacto de Emergencia" else "Nuevo Contacto de Emergencia",
                    fontWeight = FontWeight.Bold,
                    color = HighContrastWhite,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = formName,
                        onValueChange = { formName = it },
                        label = { Text("Nombre Completo") },
                        placeholder = { Text("Ej. Andrea Gómez") },
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AlertRed,
                            unfocusedBorderColor = CockpitCardBorder,
                            focusedTextColor = HighContrastWhite,
                            unfocusedTextColor = HighContrastWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = formPhone,
                        onValueChange = { formPhone = it.filter { c -> c.isDigit() || c == '+' } },
                        label = { Text("Número de Teléfono / Celular") },
                        placeholder = { Text("Ej. 5512345678") },
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AlertRed,
                            unfocusedBorderColor = CockpitCardBorder,
                            focusedTextColor = HighContrastWhite,
                            unfocusedTextColor = HighContrastWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = formRelation,
                        onValueChange = { formRelation = it },
                        label = { Text("Parentesco o Relación") },
                        placeholder = { Text("Ej. Esposa, Hermano, Asistencia Vial") },
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AlertRed,
                            unfocusedBorderColor = CockpitCardBorder,
                            focusedTextColor = HighContrastWhite,
                            unfocusedTextColor = HighContrastWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = formIsPrimary,
                            onCheckedChange = { formIsPrimary = it },
                            colors = CheckboxDefaults.colors(checkedColor = AlertRed)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Marcar como Contacto Principal (ICE)",
                            fontSize = 12.sp,
                            color = HighContrastWhite
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (formName.isNotBlank() && formPhone.isNotBlank()) {
                            if (isEditing && editingContact != null) {
                                onUpdateContact(
                                    editingContact!!.copy(
                                        name = formName.trim(),
                                        phone = formPhone.trim(),
                                        relationship = formRelation.ifBlank { "Contacto" },
                                        isPrimary = formIsPrimary
                                    )
                                )
                            } else {
                                onAddContact(
                                    formName.trim(),
                                    formPhone.trim(),
                                    formRelation.ifBlank { "Contacto" },
                                    formIsPrimary
                                )
                            }
                            isAddingNew = false
                            editingContact = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(if (isEditing) "Guardar Cambios" else "Guardar Contacto", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        isAddingNew = false
                        editingContact = null
                    }
                ) {
                    Text("Cancelar", color = Color.Gray)
                }
            },
            containerColor = CockpitCardBg
        )
    }
}
