package com.devchiradhi.rentlog.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.devchiradhi.rentlog.ui.theme.Radius
import com.devchiradhi.rentlog.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrialExpiredSheet(
    onDismiss: () -> Unit,
    onGoPremium: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = Radius.xxl,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = Spacing.xl)
                .padding(top = Spacing.sm, bottom = 100.dp), // Increased significantly to avoid home button accidental clicks
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            // Icon
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = Radius.xxl
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(Spacing.lg)
                        .size(36.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Headline
            Text(
                text = "Your 14-day trial has ended",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            // Sub-text
            Text(
                text = "Upgrade to Premium to keep using PDF reports, HRA calculator, backups, and more.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(Spacing.sm))

            // CTA
            Button(
                onClick = {
                    onDismiss()
                    onGoPremium()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = Radius.xl,
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Icon(
                    Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(Spacing.sm))
                Text(
                    "Upgrade to Premium — ₹99",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            // Dismiss
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Maybe later",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                )
            }
        }
    }
}
