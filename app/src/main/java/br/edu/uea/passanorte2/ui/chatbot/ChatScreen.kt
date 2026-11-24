package br.edu.uea.passanorte2.ui.chatbot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import br.edu.uea.passanorte2.data.remote.ChatMessageView
import br.edu.uea.passanorte2.data.remote.ItineraryItemView
import br.edu.uea.passanorte2.data.remote.ItineraryView

/**
 * Conversa com o assistente:
 *  - GET  /api/v1/chatbot/sessions/{sessionId}   (histórico)
 *  - POST /api/v1/chatbot/sessions/{sessionId}/messages (nova mensagem)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    vm: ChatViewModel,
    onBack: () -> Unit,
) {
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    // Rola para a última mensagem quando a conversa muda de tamanho.
    LaunchedEffect(vm.messages.size, vm.sending) {
        if (vm.messages.isNotEmpty()) {
            listState.animateScrollToItem(vm.messages.size)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Assistente Passanorte") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding(),
        ) {
            vm.errorMessage?.let { message ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    TextButton(onClick = { vm.loadHistory() }) { Text("Recarregar") }
                }
            }

            when {
                vm.loading -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }

                else -> MessagesList(
                    messages = vm.messages,
                    sending = vm.sending,
                    listState = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                )
            }

            MessageInput(
                draft = vm.draft,
                enabled = !vm.sending && !vm.loading,
                onDraftChange = vm::onDraftChange,
                onSend = vm::sendMessage,
            )
        }
    }
}

@Composable
private fun MessagesList(
    messages: List<ChatMessageView>,
    sending: Boolean,
    listState: androidx.compose.foundation.lazy.LazyListState,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (messages.isEmpty()) {
            item {
                Text(
                    text = "Comece pedindo um roteiro: “Sugira um itinerário para amanhã em Manaus.”",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        items(messages, key = { it.messageId }) { message ->
            MessageBubble(message)
        }
        if (sending) {
            item(key = "sending") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Assistente está digitando…",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessageView) {
    val fromTourist = message.role.isTouristRole
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (fromTourist) Alignment.End else Alignment.Start,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (fromTourist) 16.dp else 4.dp,
                        bottomEnd = if (fromTourist) 4.dp else 16.dp,
                    ),
                )
                .background(
                    if (fromTourist) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant,
                )
                .padding(12.dp),
        ) {
            Column {
                Text(message.content, style = MaterialTheme.typography.bodyMedium)
            }
        }

        // FR-025: itinerário sugerido pelo assistente, renderizado como cartão.
        message.itinerary?.let { ItineraryCard(it) }

        Text(
            text = formatTimestamp(message.createdAt),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp),
        )
    }
}

@Composable
private fun ItineraryCard(itinerary: ItineraryView) {
    Card(
        modifier = Modifier
            .widthIn(max = 340.dp)
            .padding(top = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                text = "Itinerário sugerido — " + itinerary.targetDate,
                style = MaterialTheme.typography.titleSmall,
            )
            Spacer(Modifier.height(8.dp))
            itinerary.items.forEach { item -> ItineraryItemRow(item) }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Total: " + formatCents(itinerary.totalCostCents),
                style = MaterialTheme.typography.titleSmall,
            )
        }
    }
}

@Composable
private fun ItineraryItemRow(item: ItineraryItemView) {
    Column(Modifier.padding(vertical = 4.dp)) {
        Row {
            Text(
                text = item.displayName,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = formatCents(item.ticketCostCents),
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Text(
            text = item.kind,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

@Composable
private fun MessageInput(
    draft: String,
    enabled: Boolean,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        OutlinedTextField(
            value = draft,
            onValueChange = onDraftChange,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 56.dp),
            placeholder = { Text("Digite sua mensagem…") },
            enabled = enabled,
        )
        Spacer(Modifier.width(8.dp))
        IconButton(
            onClick = onSend,
            enabled = enabled && draft.isNotBlank(),
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar")
        }
    }
}
