package chylex.hee.game.block.entity

import chylex.hee.game.block.BlockEnergyCluster
import chylex.hee.game.block.entity.TileEntityEndPortalAcceptor.ChargeState.CHARGING
import chylex.hee.game.block.entity.TileEntityEndPortalAcceptor.ChargeState.FINISHED
import chylex.hee.game.block.entity.TileEntityEndPortalAcceptor.ChargeState.IDLE
import chylex.hee.game.block.entity.TileEntityEndPortalAcceptor.ChargeState.WAITING
import chylex.hee.game.block.entity.base.TileEntityBase.Context.STORAGE
import chylex.hee.game.block.entity.base.TileEntityBasePortalController
import chylex.hee.game.block.entity.base.TileEntityBasePortalController.ForegroundRenderState.Animating
import chylex.hee.game.block.entity.base.TileEntityBasePortalController.ForegroundRenderState.Invisible
import chylex.hee.game.block.entity.base.TileEntityBasePortalController.ForegroundRenderState.Visible
import chylex.hee.game.mechanics.energy.IEnergyQuantity
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Units
import chylex.hee.game.world.util.FLAG_SKIP_RENDER
import chylex.hee.game.world.util.FLAG_SYNC_CLIENT
import chylex.hee.game.world.util.breakBlock
import chylex.hee.game.world.util.getTile
import chylex.hee.init.ModBlocks
import chylex.hee.init.ModTileEntities
import chylex.hee.util.nbt.TagCompound
import chylex.hee.util.nbt.use
import net.minecraft.tileentity.TileEntityType

class TileEntityEndPortalAcceptor(type: TileEntityType<TileEntityEndPortalAcceptor>) : TileEntityBasePortalController(type) {
	constructor() : this(ModTileEntities.END_PORTAL_ACCEPTOR)
	
	object Type : IHeeTileEntityType<TileEntityEndPortalAcceptor> {
		override val blocks
			get() = arrayOf(ModBlocks.END_PORTAL_ACCEPTOR)
	}
	
	private companion object {
		private const val NO_REFRESH = Int.MAX_VALUE
		
		private const val WAITING_REFRESH_RATE = 20
		private const val CHARGING_REFRESH_RATE = 2
		
		private const val ENERGY_CHARGE_TAG = "EnergyCharge"
		
		private val ENERGY_REQUIRED = Units(100)
		private val ENERGY_PER_UPDATE = Units(1)
		
		private val ANIMATION_PROGRESS_PER_UPDATE = 1F / (CHARGING_REFRESH_RATE.toFloat() * ENERGY_REQUIRED.units.value / ENERGY_PER_UPDATE.units.value)
	}
	
	// Client animation
	
	override val serverRenderState
		get() = when (chargeState) {
			IDLE, WAITING -> Invisible
			CHARGING      -> Animating(chargedEnergy.units.value.toFloat() / ENERGY_REQUIRED.units.value)
			FINISHED      -> Visible
		}
	
	override val clientAnimationFadeInSpeed
		get() = ANIMATION_PROGRESS_PER_UPDATE
	
	override val clientAnimationFadeOutSpeed
		get() = ANIMATION_PROGRESS_PER_UPDATE * 8F
	
	// Charge handling
	
	private enum class ChargeState(val refreshRate: Int) {
		IDLE(NO_REFRESH),
		WAITING(WAITING_REFRESH_RATE),
		CHARGING(CHARGING_REFRESH_RATE),
		FINISHED(NO_REFRESH)
	}
	
	val isCharged
		get() = chargedEnergy >= ENERGY_REQUIRED
	
	private var chargeState = IDLE
	private var ticksToRefresh = NO_REFRESH
	private var isRefreshing = false
	
	private var chargedEnergy: IEnergyQuantity = Units(0)
	
	fun refreshClusterState() {
		if (wrld.isRemote || isRefreshing) {
			return
		}
		
		isRefreshing = true // required to ignore a neighborChanged call when breaking the Cluster
		
		val newChargeState: ChargeState
		
		val posAbove = pos.up()
		val cluster = posAbove.getTile<TileEntityEnergyCluster>(wrld)
		
		if (cluster == null) {
			newChargeState = when (chargeState) {
				FINISHED -> FINISHED
				IDLE     -> IDLE
				WAITING  -> IDLE
				
				CHARGING -> {
					BlockEnergyCluster.createSmallLeak(wrld, posAbove, chargedEnergy)
					chargedEnergy = Units(0)
					IDLE
				}
			}
		}
		else {
			newChargeState = when (chargeState) {
				FINISHED -> FINISHED
				IDLE     -> WAITING
				
				WAITING  ->
					if (cluster.energyLevel >= ENERGY_REQUIRED)
						CHARGING
					else
						WAITING
				
				CHARGING ->
					if (cluster.drainEnergy(ENERGY_PER_UPDATE)) {
						chargedEnergy += ENERGY_PER_UPDATE
						
						if (chargedEnergy >= ENERGY_REQUIRED) {
							cluster.breakWithoutExplosion = true
							cluster.pos.breakBlock(wrld, false)
							FINISHED
						}
						else {
							markDirty()
							CHARGING
						}
					}
					else {
						cluster.breakWithoutExplosion = true
						cluster.pos.breakBlock(wrld, false)
						CHARGING
					}
			}
		}
		
		if (chargeState != newChargeState) {
			chargeState = newChargeState
			notifyUpdate(FLAG_SYNC_CLIENT or FLAG_SKIP_RENDER)
			markDirty()
		}
		
		isRefreshing = false
		ticksToRefresh = chargeState.refreshRate
	}
	
	fun toggleChargeFromCreativeMode() {
		if (chargeState == FINISHED) {
			chargeState = IDLE
			chargedEnergy = Units(0)
		}
		else {
			chargeState = FINISHED
			chargedEnergy = ENERGY_REQUIRED
		}
		
		notifyUpdate(FLAG_SYNC_CLIENT or FLAG_SKIP_RENDER)
		markDirty()
	}
	
	// Overrides
	
	override fun firstTick() {
		refreshClusterState()
	}
	
	override fun tick() {
		super.tick()
		
		if (!wrld.isRemote && ticksToRefresh != NO_REFRESH && --ticksToRefresh <= 0) {
			refreshClusterState()
		}
	}
	
	override fun writeNBT(nbt: TagCompound, context: Context) = nbt.use {
		super.writeNBT(nbt, context)
		
		if (context == STORAGE) {
			putInt(ENERGY_CHARGE_TAG, chargedEnergy.units.value)
		}
	}
	
	override fun readNBT(nbt: TagCompound, context: Context) = nbt.use {
		super.readNBT(nbt, context)
		
		if (context == STORAGE) {
			chargedEnergy = Units(getInt(ENERGY_CHARGE_TAG))
			
			if (chargedEnergy >= ENERGY_REQUIRED) {
				chargeState = FINISHED
			}
			else if (chargedEnergy > Units(0)) {
				chargeState = CHARGING
			}
		}
	}
}
