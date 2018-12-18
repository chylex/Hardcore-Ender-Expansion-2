package chylex.hee.init
import chylex.hee.HEE
import chylex.hee.game.block.entity.TileEntityLootChest
import chylex.hee.game.gui.ContainerAmuletOfRecovery
import chylex.hee.game.gui.ContainerLootChest
import chylex.hee.game.gui.ContainerTrinketPouch
import chylex.hee.game.gui.GuiAmuletOfRecovery
import chylex.hee.game.gui.GuiLootChest
import chylex.hee.game.gui.GuiTrinketPouch
import chylex.hee.system.util.Pos
import chylex.hee.system.util.getTile
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumHand
import net.minecraft.world.World
import net.minecraftforge.fml.common.network.IGuiHandler
import net.minecraftforge.fml.common.network.NetworkRegistry

object ModGuiHandler : IGuiHandler{
	fun initialize(){
		NetworkRegistry.INSTANCE.registerGuiHandler(HEE, this)
	}
	
	// Types
	
	enum class GuiType(
		val createInterface: (player: EntityPlayer, Int, Int, Int) -> Any?,
		val createContainer: (player: EntityPlayer, Int, Int, Int) -> Any?
	){
		AMULET_OF_RECOVERY(
			createInterface = { player, hand, _, _ -> GuiAmuletOfRecovery(player, EnumHand.values()[hand]) },
			createContainer = { player, hand, _, _ -> ContainerAmuletOfRecovery(player, EnumHand.values()[hand]) }
		),
		
		TRINKET_POUCH(
			createInterface = { player, slot, _, _ -> GuiTrinketPouch(player, slot) },
			createContainer = { player, slot, _, _ -> ContainerTrinketPouch(player, slot) }
		),
		
		LOOT_CHEST(
			createInterface = forTileEntity<TileEntityLootChest> { player, tile -> GuiLootChest(player, tile) },
			createContainer = forTileEntity<TileEntityLootChest> { player, tile -> ContainerLootChest(player, tile) }
		);
		
		fun open(player: EntityPlayer, x: Int = 0, y: Int = 0, z: Int = 0){
			player.openGui(HEE, ordinal, player.world, x, y, z)
		}
	}
	
	// Utilities
	
	private inline fun <reified T : TileEntity> forTileEntity(crossinline mapTo: (EntityPlayer, T) -> Any): (player: EntityPlayer, Int, Int, Int) -> Any?{
		return { player, x, y, z -> Pos(x, y, z).getTile<T>(player.world)?.let { mapTo(player, it) } }
	}
	
	// Overrides
	
	override fun getClientGuiElement(id: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int) =
		GuiType.values().getOrNull(id)?.createInterface?.invoke(player, x, y, z)
	
	override fun getServerGuiElement(id: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int) =
		GuiType.values().getOrNull(id)?.createContainer?.invoke(player, x, y, z)
}
