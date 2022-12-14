import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.apache.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import kotlinx.coroutines.runBlocking
import kotlin.math.floor
import kotlin.math.roundToInt


val client = HttpClient(Apache) {
    install(ContentNegotiation) {
        jackson(ContentType.Any)
    }
}

val amountOfCoinsAvailable = 17_000_000

runBlocking {
    val latestPriceData: PriceData = client.get("https://prices.runescape.wiki/api/v1/osrs/latest").body()
    val latestMapping: List<MappingData> = client.get("https://prices.runescape.wiki/api/v1/osrs/mapping").body()
    val latestVolumesData: VolumeData = client.get("https://prices.runescape.wiki/api/v1/osrs/volumes").body()
    val recipes: List<Recipe> =
        client.get("https://raw.githubusercontent.com/Flipping-Utilities/osrs-datasets/master/recipes.json").body()
    val potions: List<Potion> =
        client.get("https://raw.githubusercontent.com/Flipping-Utilities/osrs-datasets/master/potions.json").body()
    val combinedDataOfAllItems = latestPriceData.data.entries
        .map { sortedSet ->
            CombinedData(
                sortedSet.key.toInt(),
                latestMapping.firstOrNull { it.id == sortedSet.key.toInt() }?.name,
                (latestVolumesData.data[sortedSet.key] ?: 0),
                latestMapping.firstOrNull { it.id == sortedSet.key.toInt() }?.limit?.takeIf { it > 0 } ?: floor(amountOfCoinsAvailable / sortedSet.value.low).toInt(),
                sortedSet.value.roi,
                sortedSet.value.low,
                sortedSet.value.high
            )
        }.filter { it.name != null } + CombinedData(995, "coins", 0, Int.MAX_VALUE, 0.0, 1.0, 1.0)

    println(
        combinedDataOfAllItems
            .asSequence()
            .sortedByDescending { minOf(floor(amountOfCoinsAvailable / it.lowPrice), it.limit.toDouble()) * (it.highPrice - it.lowPrice) - (it.highPrice/100) }
            .filter { (it.lowPrice > 500_000 && it.volume > 50L) || (it.lowPrice > 100 && it.volume > 50_000L) }
            .filter { it.roi > 2 }
            .joinToString(separator = "") {
                "$it - potentialProfit=${(it.limit * (it.highPrice - it.lowPrice)) - (it.highPrice/100)}, usableProfit=${(minOf(floor(amountOfCoinsAvailable / it.lowPrice), it.limit.toDouble()) * (it.highPrice - it.lowPrice)) - (it.highPrice/100)}\n"
            }
    )

    val recipeDataOfAllRecipes = recipes.map { recipe ->
            RecipeData(
                recipe.name,
                recipe.outputs.associate { output -> combinedDataOfAllItems.first { it.id == output.id } to output.quantity },
                recipe.inputs.associate { input -> combinedDataOfAllItems.first { it.id == input.id } to input.quantity }
            )
        }
    println("-----------------------------------------------------------------------------")
    println("---------------------------------RECIPES-------------------------------------")
    println("-----------------------------------------------------------------------------")

    println(
        recipeDataOfAllRecipes
            .asSequence()
            .sortedByDescending { it.potentialProfit }
            .filter { it.input.isNotEmpty() }
            .filter { it.roi > 3 }
            .filter { it.output.all { it.key.lowPrice > 150_000 && it.key.volume > 30L } }
            .filter { it.input.all { it.key.volume > 20L } }
            .joinToString("\n")
    )

    val potionDataOfAllPotions = potions.filter { it.doses.any { it.dose == 4 } }.map { potion ->
        PotionData(
            potion.name,
            potion.doses.first { it.dose == 3}.let { dose -> combinedDataOfAllItems.first { it.id == dose.id } },
            potion.doses.first { it.dose == 4}.let { dose -> combinedDataOfAllItems.first { it.id == dose.id } },
        )
    }
    println("-----------------------------------------------------------------------------")
    println("---------------------------------POTIONS-------------------------------------")
    println("-----------------------------------------------------------------------------")
    println(
        potionDataOfAllPotions
            .asSequence()
            .sortedByDescending { it.potentialProfit }
            .filter { it.roi > 2 }
            .filter { it.dose3.volume > 100_000 }
            .joinToString("\n")
    )
}

