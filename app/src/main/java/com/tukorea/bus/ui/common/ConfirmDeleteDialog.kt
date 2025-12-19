package com.tukorea.bus.ui.common

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tukorea.bus.R
import com.tukorea.bus.ui.theme.Gray700
import com.tukorea.bus.ui.theme.PrimaryBlue

/**
 * 재사용 가능한 확인 다이얼로그
 *
 * @param showDialog 다이얼로그 표시 여부
 * @param onDismiss 취소 또는 dismiss 시 호출되는 콜백
 * @param onConfirm 확인 시 호출되는 콜백
 * @param title 다이얼로그 제목
 * @param message 다이얼로그 메시지
 * @param icon 다이얼로그 아이콘 (선택적)
 * @param confirmButtonText 확인 버튼 텍스트 (기본값: "삭제")
 * @param dismissButtonText 취소 버튼 텍스트 (기본값: "취소")
 * @param confirmButtonColor 확인 버튼 색상 (기본값: error - 빨간색)
 */
@Composable
fun ConfirmDeleteDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title: String = stringResource(id = R.string.calendar_delete_title),
    message: String = stringResource(id = R.string.calendar_delete_message),
    icon: ImageVector? = Icons.Default.DateRange,
    confirmButtonText: String = stringResource(id = R.string.common_delete),
    dismissButtonText: String = stringResource(id = R.string.common_cancel),
    confirmButtonColor: Color? = null
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = Color.White,
            icon = icon?.let {
                {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = PrimaryBlue
                    )
                }
            },
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray700
                )
            },
            confirmButton = {
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = confirmButtonColor ?: MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(confirmButtonText)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = onDismiss
                ) {
                    Text(dismissButtonText)
                }
            }
        )
    }
}
