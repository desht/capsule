const csv = `net.minecraft.advancements.criterion.EntityTypeUtil,net.minecraft.advancements.criterion.EntityTypePredicate
net.minecraft.block.Block$Builder,net.minecraft.block.Block$Properties
net.minecraft.block.BlockIronBars,net.minecraft.block.BlockPane
net.minecraft.block.material.MapColor,net.minecraft.block.material.MaterialColor
net.minecraft.client.audio.PositionedSound,net.minecraft.client.audio.AbstractSound
net.minecraft.client.audio.PositionedSoundRecord,net.minecraft.client.audio.SimpleSound
net.minecraft.client.renderer.EntityRenderer,net.minecraft.client.renderer.GameRenderer
net.minecraft.client.renderer.RenderGlobal,net.minecraft.client.renderer.WorldRenderer
net.minecraft.client.renderer.block.model.BakedQuad,net.minecraft.client.renderer.model.BakedQuad
net.minecraft.client.renderer.block.model.BakedQuadRetextured,net.minecraft.client.renderer.model.BakedQuadRetextured
net.minecraft.client.renderer.block.model.BlockFaceUV,net.minecraft.client.renderer.model.BlockFaceUV
net.minecraft.client.renderer.block.model.BlockPart,net.minecraft.client.renderer.model.BlockPart
net.minecraft.client.renderer.block.model.BlockPartFace,net.minecraft.client.renderer.model.BlockPartFace
net.minecraft.client.renderer.block.model.BlockPartRotation,net.minecraft.client.renderer.model.BlockPartRotation
net.minecraft.client.renderer.block.model.BuiltInModel,net.minecraft.client.renderer.model.BuiltInModel
net.minecraft.client.renderer.block.model.FaceBakery,net.minecraft.client.renderer.model.FaceBakery
net.minecraft.client.renderer.block.model.IBakedModel,net.minecraft.client.renderer.model.IBakedModel
net.minecraft.client.renderer.block.model.IUnbakedModel,net.minecraft.client.renderer.model.IUnbakedModel
net.minecraft.client.renderer.block.model.ItemCameraTransforms,net.minecraft.client.renderer.model.ItemCameraTransforms
net.minecraft.client.renderer.block.model.ItemModelGenerator,net.minecraft.client.renderer.model.ItemModelGenerator
net.minecraft.client.renderer.block.model.ItemOverride,net.minecraft.client.renderer.model.ItemOverride
net.minecraft.client.renderer.block.model.ItemOverrideList,net.minecraft.client.renderer.model.ItemOverrideList
net.minecraft.client.renderer.block.model.ItemTransformVec3f,net.minecraft.client.renderer.model.ItemTransformVec3f
net.minecraft.client.renderer.block.model.ModelBakery,net.minecraft.client.renderer.model.ModelBakery
net.minecraft.client.renderer.block.model.ModelBlock,net.minecraft.client.renderer.model.ModelBlock
net.minecraft.client.renderer.block.model.ModelBlockDefinition,net.minecraft.client.renderer.model.ModelBlockDefinition
net.minecraft.client.renderer.block.model.ModelManager,net.minecraft.client.renderer.model.ModelManager
net.minecraft.client.renderer.block.model.ModelResourceLocation,net.minecraft.client.renderer.model.ModelResourceLocation
net.minecraft.client.renderer.block.model.ModelRotation,net.minecraft.client.renderer.model.ModelRotation
net.minecraft.client.renderer.block.model.MultipartBakedModel,net.minecraft.client.renderer.model.MultipartBakedModel
net.minecraft.client.renderer.block.model.SimpleBakedModel,net.minecraft.client.renderer.model.SimpleBakedModel
net.minecraft.client.renderer.block.model.Variant,net.minecraft.client.renderer.model.Variant
net.minecraft.client.renderer.block.model.VariantList,net.minecraft.client.renderer.model.VariantList
net.minecraft.client.renderer.block.model.WeightedBakedModel,net.minecraft.client.renderer.model.WeightedBakedModel
net.minecraft.client.renderer.block.model.multipart.ConditionAnd,net.minecraft.client.renderer.model.multipart.AndCondition
net.minecraft.client.renderer.block.model.multipart.ConditionOr,net.minecraft.client.renderer.model.multipart.OrCondition
net.minecraft.client.renderer.block.model.multipart.ConditionPropertyValue,net.minecraft.client.renderer.model.multipart.PropertyValueCondition
net.minecraft.client.renderer.block.model.multipart.ICondition,net.minecraft.client.renderer.model.multipart.ICondition
net.minecraft.client.renderer.block.model.multipart.Multipart,net.minecraft.client.renderer.model.multipart.Multipart
net.minecraft.client.renderer.block.model.multipart.Selector,net.minecraft.client.renderer.model.multipart.Selector
net.minecraft.item.Item$Builder,net.minecraft.item.Item$Properties
net.minecraft.network.play.server.SPacketUpdateRecipesPacket,net.minecraft.network.play.server.SPacketUpdateRecipes
net.minecraft.scoreboard.IScoreCriteria,net.minecraft.scoreboard.ScoreCriteria
net.minecraft.scoreboard.IScoreCriteria$EnumRenderType,net.minecraft.scoreboard.ScoreCriteria$RenderType
net.minecraft.server.network.NetHandlerHandshakeTCP,net.minecraft.network.NetHandlerHandshakeTCP
net.minecraft.server.network.NetHandlerLoginServer,net.minecraft.network.NetHandlerLoginServer
net.minecraft.server.network.NetHandlerStatusServer,net.minecraft.network.NetHandlerStatusServer
net.minecraft.stats.StatBase,net.minecraft.stats.Stat
net.minecraft.util.math.shapes.ShapeUtils,net.minecraft.util.math.shapes.VoxelShapes
net.minecraft.world.ChunkCache,net.minecraft.world.Region
net.minecraft.world.ColorizerFoliage,net.minecraft.world.FoliageColors
net.minecraft.world.ColorizerGrass,net.minecraft.world.GrassColors
net.minecraft.world.biome.BiomeColorHelper,net.minecraft.world.biome.BiomeColors
net.minecraft.world.gen.feature.GlowStoneFeature,net.minecraft.world.gen.feature.GlowstoneFeature`

const replace = require('replace-in-file');

const tuples = csv.split('\n').map(tuple => tuple.split(','))
for (const [from, to] of tuples) {
    const options = {
        files: [
            './src/**/*.java',
        ],
        from,
        to
    };
    replace(options)
        .then(results => {
            if (results.hasChanged) console.log('Changed:', results.file);
        })
        .catch(error => {
            console.error('Error occurred:', error);
        });
}