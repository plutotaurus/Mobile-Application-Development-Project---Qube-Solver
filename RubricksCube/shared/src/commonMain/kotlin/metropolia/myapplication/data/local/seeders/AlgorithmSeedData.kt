package metropolia.myapplication.data.local.seeders

import metropolia.myapplication.data.local.AlgorithmDao
import metropolia.myapplication.data.local.AlgorithmEntity
import metropolia.myapplication.data.local.AlgorithmVariantEntity
import metropolia.myapplication.data.local.UserEntity

private const val LIBRARY_USER_ID = "local-library"

private val libraryUser = UserEntity(
    userId = LIBRARY_USER_ID,
    username = "Cube Library"
)

// PLL cases and sequences from https://jperm.net/algs/pll.
// Keep IDs stable and append new versions so existing installs receive new cases.
private val catalogVersions = listOf(
    "algorithm_catalog_v1" to listOf(
        AlgorithmEntity(
            algorithmId = "T-perm",
            userId = LIBRARY_USER_ID,
            type = "PLL",
            algorithmText = "R U R' U' R' F R2 U' R' U' R U R' F'",
        ),
    ),
    "algorithm_catalog_v2" to listOf(
        AlgorithmEntity(
            algorithmId = "Aa-perm",
            userId = LIBRARY_USER_ID,
            type = "PLL",
            algorithmText = "x L2 D2 L' U' L D2 L' U L'",
        ),
        AlgorithmEntity(
            algorithmId = "Ab-perm",
            userId = LIBRARY_USER_ID,
            type = "PLL",
            algorithmText = "x' L2 D2 L U L' D2 L U' L",
        ),
        AlgorithmEntity(
            algorithmId = "E-perm",
            userId = LIBRARY_USER_ID,
            type = "PLL",
            algorithmText = "x' L' U L D' L' U' L D L' U' L D' L' U L D",
        ),
        AlgorithmEntity(
            algorithmId = "F-perm",
            userId = LIBRARY_USER_ID,
            type = "PLL",
            algorithmText = "R' U' F' R U R' U' R' F R2 U' R' U' R U R' U R",
        ),
        AlgorithmEntity(
            algorithmId = "Ga-perm",
            userId = LIBRARY_USER_ID,
            type = "PLL",
            algorithmText = "R2 U R' U R' U' R U' R2 U' D R' U R D'",
        ),
        AlgorithmEntity(
            algorithmId = "Gb-perm",
            userId = LIBRARY_USER_ID,
            type = "PLL",
            algorithmText = "R' U' R U D' R2 U R' U R U' R U' R2 D",
        ),
        AlgorithmEntity(
            algorithmId = "Gc-perm",
            userId = LIBRARY_USER_ID,
            type = "PLL",
            algorithmText = "R2 U' R U' R U R' U R2 U D' R U' R' D",
        ),
        AlgorithmEntity(
            algorithmId = "Gd-perm",
            userId = LIBRARY_USER_ID,
            type = "PLL",
            algorithmText = "R U R' U' D R2 U' R U' R' U R' U R2 D'",
        ),
        AlgorithmEntity(
            algorithmId = "H-perm",
            userId = LIBRARY_USER_ID,
            type = "PLL",
            algorithmText = "M2 U M2 U2 M2 U M2",
        ),
        AlgorithmEntity(
            algorithmId = "Ja-perm",
            userId = LIBRARY_USER_ID,
            type = "PLL",
            algorithmText = "x R2 F R F' R U2 r' U r U2",
        ),
        AlgorithmEntity(
            algorithmId = "Jb-perm",
            userId = LIBRARY_USER_ID,
            type = "PLL",
            algorithmText = "R U R' F' R U R' U' R' F R2 U' R'",
        ),
        AlgorithmEntity(
            algorithmId = "Na-perm",
            userId = LIBRARY_USER_ID,
            type = "PLL",
            algorithmText = "R U R' U R U R' F' R U R' U' R' F R2 U' R' U2 R U' R'",
        ),
       AlgorithmEntity(
            algorithmId = "Nb-perm",
            userId = LIBRARY_USER_ID,
            type = "PLL",
            algorithmText = "R' U R U' R' F' U' F R U R' F R' F' R U' R",
        ),
        AlgorithmEntity(
            algorithmId = "Ra-perm",
            userId = LIBRARY_USER_ID,
            type = "PLL",
            algorithmText = "R U' R' U' R U R D R' U' R D' R' U2 R'",
        ),
        AlgorithmEntity(
            algorithmId = "Rb-perm",
            userId = LIBRARY_USER_ID,
            type = "PLL",
            algorithmText = "R2 F R U R U' R' F' R U2 R' U2 R",
        ),
        AlgorithmEntity(
            algorithmId = "Ua-perm",
            userId = LIBRARY_USER_ID,
            type = "PLL",
            algorithmText = "M2 U M U2 M' U M2",
        ),
        AlgorithmEntity(
            algorithmId = "Ub-perm",
            userId = LIBRARY_USER_ID,
            type = "PLL",
            algorithmText = "M2 U' M U2 M' U' M2",
        ),
        AlgorithmEntity(
            algorithmId = "V-perm",
            userId = LIBRARY_USER_ID,
            type = "PLL",
            algorithmText = "R' U R' U' y R' F' R2 U' R' U R' F R F",
        ),
        AlgorithmEntity(
            algorithmId = "Y-perm",
            userId = LIBRARY_USER_ID,
            type = "PLL",
            algorithmText = "F R U' R' U' R U R' F' R U R' U' R' F R F'",
        ),
        AlgorithmEntity(
            algorithmId = "Z-perm",
            userId = LIBRARY_USER_ID,
            type = "PLL",
            algorithmText = "M' U M2 U M2 U M' U2 M2",
        ),
    ),
    "algorithm_catalog_v3" to ollSeedMoves.map { (caseNumber, moves) ->
        AlgorithmEntity(
            algorithmId = caseNumber.toString(),
            userId = LIBRARY_USER_ID,
            type = "OLL",
            algorithmText = moves,
        )
    },
)

private val starterVariants = listOf(
    AlgorithmVariantEntity(
        "H-perm",
        "M2 U' M2 U2 M2 U' M2"
    ),
    AlgorithmVariantEntity(
        "Ua-perm",
        "R U' R U R U R U' R' U' R2"
    ),
    AlgorithmVariantEntity(
        "Ub-perm",
        "R2 U R U R' U' R' U' R' U R'"
    ),
    AlgorithmVariantEntity(
        "Z-perm",
        "M2 U2 M U M2 U M2 U M"
    ),
)

internal suspend fun AlgorithmDao.seedStarterAlgorithms() {
    for ((versionKey, algorithms) in catalogVersions) {
        importStarterCatalog(versionKey, libraryUser, algorithms)
    }
    importStarterVariants(LIBRARY_USER_ID, starterVariants)
}
