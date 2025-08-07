data class PriceData(
    val data: Map<String, Price>
)

data class Price(
    val high: Double,
    val highTime: Long,
    val low: Double,
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
    val lowPrice: Double,
    val highPrice: Double
)

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
    val margin: Double = output.entries.sumOf { it.key.highPrice * 0.98 * it.value } - input.entries.sumOf { it.key.lowPrice * it.value },
    val potentialProfit: Double =
            input.entries
                .ifEmpty { null }
                ?.filter { it.value > 0 }
                ?.minOf { minOf(it.key.limit / it.value, it.key.volume.toInt()) }
                ?.let { it * margin }
                ?: (margin * output.entries.sumOf { it.key.volume })
)

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
)

fun calculateRoi(highPrice: Double, lowPrice: Double): Double =
    ((highPrice / lowPrice) - 1) * 100