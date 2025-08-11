data class PriceData(
    val data: Map<String, Price>
)

data class Price(
    val high: Long,
    val highTime: Long,
    val low: Long,
    val lowTime: Long,
    val roi: Double = calculateRoi(high, low)
)

data class VolumeData(
    val timestamp: Long,
    val data: Map<String, Long>
)

data class MappingData(
    val id: Int,
    val name: String,
    val examine: String,
    val members: Boolean,
    val lowalch: Int,
    val limit: Int,
    val value: Int,
    val highalch: Int,
    val icon: String
)

data class CombinedData(
    val id: Int,
    val name: String?,
    val volume: Long,
    val limit: Int,
    val roi: Double,
    val lowPrice: Long,
    val highPrice: Long
) {
    override fun toString(): String {
        return "$name, lowPrice: $lowPrice, highPrice: $highPrice, volume: $volume, limit: $limit"
    }
}

data class Recipe(
    val name: String,
    val outputs: List<Ingredient>,
    val inputs: List<Ingredient>
)

data class Ingredient(
    val id: Int,
    val quantity: Int,
    val subText: String?,
    val cost: Double,
    val notes: String?,
    val text: String?
)

data class RecipeData(
    val recipeName: String,
    val output: Map<CombinedData, Int>,
    val input: Map<CombinedData, Int>,
    val roi: Double = calculateRoi(output.entries.sumOf { it.key.highPrice * it.value }, input.entries.sumOf { it.key.lowPrice * it.value }),
    val margin: Long = (output.entries.sumOf { it.key.highPrice * 0.98 * it.value } - input.entries.sumOf { it.key.lowPrice * it.value }).toLong(),
    val potentialProfit: Long =
            input.entries
                .ifEmpty { null }
                ?.filter { it.value > 0 }
                ?.minOf { minOf(it.key.limit / it.value, it.key.volume.toInt()/24) }
                ?.let { it * margin }
                ?: (margin * output.entries.sumOf { it.key.volume/24 })
) {
    override fun toString(): String {
        return "$recipeName | potentialProfit: $potentialProfit, margin: $margin \n\r\tinput: ${input.entries.joinToString("") { "\n\r\t\t${it.value}x ${it.key}" }} \n\r\toutput: ${output.entries.joinToString("") { "\n\r\t\t${it.value}x ${it.key}" }} \n\r"
    }
}

data class Potion(
    val name: String,
    val doses: List<PotionDose>
)

data class PotionDose(
    val id: Int,
    val dose: Int
)

data class PotionData(
    val potionName: String,
    val dose3: CombinedData,
    val dose4: CombinedData,
    val roi: Double = calculateRoi(dose4.highPrice, (dose3.lowPrice/3)*4),
    val margin: Double = (dose4.highPrice * 0.98) - (dose3.lowPrice/3)*4,
    val potentialProfit: Double = ((dose3.limit/4) * 3 * margin)
) {
    override fun toString(): String {
        return "$potionName | potentialProfit: $potentialProfit, margin: $margin \n\r\tdose3: ($dose3) \n\r\tdose4: ($dose4) \n\r"
    }
}

fun calculateRoi(highPrice: Long, lowPrice: Long): Double =
    ((highPrice.toDouble() / lowPrice) - 1) * 100