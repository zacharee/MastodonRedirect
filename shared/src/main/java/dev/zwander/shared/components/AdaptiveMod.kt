package dev.zwander.shared.components

import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.util.Objects

class AdaptiveMod(private val minSize: Dp, private val itemCount: Int) : StaggeredGridCells {
    init {
        require(minSize > 0.dp)
    }

    override fun Density.calculateCrossAxisCellSizes(
        availableSize: Int,
        spacing: Int
    ): IntArray {
        val count = maxOf(minOf((availableSize + spacing) / (minSize.roundToPx() + spacing), itemCount), 1)
        return calculateCellsCrossAxisSizeImpl(availableSize, count, spacing)
    }

    override fun hashCode(): Int {
        return Objects.hash(minSize, itemCount)
    }

    override fun equals(other: Any?): Boolean {
        return other is AdaptiveMod
                && minSize == other.minSize
                && itemCount == other.itemCount
    }
}

private fun calculateCellsCrossAxisSizeImpl(
    gridSize: Int,
    slotCount: Int,
    spacing: Int
): IntArray {
    val gridSizeWithoutSpacing = gridSize - spacing * (slotCount - 1)
    val slotSize = gridSizeWithoutSpacing / slotCount
    val remainingPixels = gridSizeWithoutSpacing % slotCount
    return IntArray(slotCount) {
        slotSize + if (it < remainingPixels) 1 else 0
    }
}