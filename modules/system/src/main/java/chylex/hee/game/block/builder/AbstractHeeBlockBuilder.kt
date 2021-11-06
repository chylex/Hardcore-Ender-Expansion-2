package chylex.hee.game.block.builder

import chylex.hee.client.text.LocalizationStrategy
import chylex.hee.game.block.IHeeBlock
import chylex.hee.game.block.properties.BlockDrop
import chylex.hee.game.block.properties.BlockHardness
import chylex.hee.game.block.properties.BlockHarvestTool
import chylex.hee.game.block.properties.BlockRenderLayer
import chylex.hee.game.block.properties.BlockTint
import chylex.hee.game.block.properties.IBlockStateModelSupplier
import net.minecraft.block.AbstractBlock.IPositionPredicate
import net.minecraft.block.AbstractBlock.Properties
import net.minecraft.block.Block
import net.minecraft.block.SoundType
import net.minecraft.block.material.Material
import net.minecraft.block.material.MaterialColor
import net.minecraft.tags.ITag.INamedTag

abstract class AbstractHeeBlockBuilder<T : Block> {
	private companion object {
		private val DEFAULT_MATERIAL = Material.Builder(MaterialColor.AIR).build()
		
		private fun always(value: Boolean): IPositionPredicate {
			return IPositionPredicate { _, _, _ -> value }
		}
	}
	
	var localization: LocalizationStrategy? = null
	val localizationExtra = mutableMapOf<String, String>()
	
	var model: IBlockStateModelSupplier? = null
	var renderLayer: BlockRenderLayer? = null
	
	var material: Material? = null
	var color: MaterialColor? = null
	var sound: SoundType? = null
	var tint: BlockTint? = null
	var light: Int? = null
	
	var isSolid: Boolean? = null
	var isOpaque: Boolean? = null
	var suffocates: Boolean? = null
	var blocksVision: Boolean? = null
	
	var drop: BlockDrop? = null
	var tool: BlockHarvestTool? = null
	var hardness: BlockHardness? = null
	
	private val lazyComponents = lazy(::HeeBlockComponents)
	val components
		get() = lazyComponents.value
	
	val tags = mutableListOf<INamedTag<Block>>()
	val interfaces = HeeBlockInterfaces()
	
	fun includeFrom(source: AbstractHeeBlockBuilder<*>) {
		source.localization?.let { this.localization = it }
		this.localizationExtra.putAll(source.localizationExtra)
		
		source.model?.let { this.model = it }
		source.renderLayer?.let { this.renderLayer = it }
		
		source.material?.let { this.material = it }
		source.color?.let { this.color = it }
		source.sound?.let { this.sound = it }
		source.tint?.let { this.tint = it }
		source.light?.let { this.light = it }
		
		source.isSolid?.let { this.isSolid = it }
		source.isOpaque?.let { this.isOpaque = it }
		source.suffocates?.let { this.suffocates = it }
		source.blocksVision?.let { this.blocksVision = it }
		
		source.drop?.let { this.drop = it }
		source.tool?.let { this.tool = it }
		source.hardness?.let { this.hardness = it }
		
		if (source.lazyComponents.isInitialized()) {
			this.components.includeFrom(source.components)
		}
		
		this.tags.addAll(source.tags)
		this.interfaces.includeFrom(source.interfaces)
	}
	
	protected val heeDelegate
		get() = object : IHeeBlock {
			override val localization = this@AbstractHeeBlockBuilder.localization ?: super.localization
			override val localizationExtra = this@AbstractHeeBlockBuilder.localizationExtra.toMap()
			override val model = this@AbstractHeeBlockBuilder.model ?: super.model
			override val renderLayer = this@AbstractHeeBlockBuilder.renderLayer ?: super.renderLayer
			override val tint = this@AbstractHeeBlockBuilder.tint
			override val drop = this@AbstractHeeBlockBuilder.drop ?: super.drop
			override val tags = this@AbstractHeeBlockBuilder.tags.toList()
		}
	
	private fun buildProperties(setup: ((Properties) -> Properties)?): Properties {
		val material = material ?: DEFAULT_MATERIAL
		val color = color ?: material.color
		val tool = tool ?: BlockHarvestTool.NONE
		
		var properties = Properties.create(material, color)
		
		if (setup != null) {
			properties = setup(properties)
		}
		
		properties = tool.applyTo(properties)
		properties = properties.apply(sound, Properties::sound)
		properties = properties.apply(light) { level -> setLightLevel { level } }
		properties = properties.apply(isOpaque) { setOpaque(always(it)) }
		properties = properties.apply(suffocates) { setSuffocates(always(it)) }
		properties = properties.apply(blocksVision) { setBlocksVision(always(it)) }
		properties = properties.apply(hardness) { it.applyTo(this) }
		
		if (isSolid == false) {
			properties = properties.notSolid()
		}
		
		if (!material.blocksMovement()) {
			if (isSolid == true) {
				throw UnsupportedOperationException("[AbstractHeeBlockBuilder] cannot create a block that does not block movement and is solid at the same time")
			}
			
			properties = properties.doesNotBlockMovement()
		}
		
		if (drop === BlockDrop.Nothing) {
			properties = properties.noDrops()
		}
		
		if (lazyComponents.isInitialized() && components.randomTick != null) {
			properties = properties.tickRandomly()
		}
		
		return properties
	}
	
	private inline fun <T> Properties.apply(value: T?, function: Properties.(T) -> Properties): Properties {
		return if (value == null) this else function(value)
	}
	
	fun build(propertiesSetup: (Properties.() -> Properties)? = null): T {
		val components = if (lazyComponents.isInitialized()) components else null
		return buildBlock(buildProperties(propertiesSetup), components)
	}
	
	internal abstract fun buildBlock(properties: Properties, components: HeeBlockComponents?): T
}
