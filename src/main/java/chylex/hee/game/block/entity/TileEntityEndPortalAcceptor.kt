package chylex.hee.game.block.entity
import chylex.hee.game.block.BlockEnergyCluster
import chylex.hee.game.block.entity.TileEntityBase.Context.STORAGE
import chylex.hee.game.block.entity.TileEntityBasePortalController.ForegroundRenderState.Animating
import chylex.hee.game.block.entity.TileEntityBasePortalController.ForegroundRenderState.Invisible
import chylex.hee.game.block.entity.TileEntityBasePortalController.ForegroundRenderState.Visible
import chylex.hee.game.block.entity.TileEntityEndPortalAcceptor.ChargeState.CHARGING
import chylex.hee.game.block.entity.TileEntityEndPortalAcceptor.ChargeState.FINISHED
import chylex.hee.game.block.entity.TileEntityEndPortalAcceptor.ChargeState.IDLE
import chylex.hee.game.block.entity.TileEntityEndPortalAcceptor.ChargeState.WAITING
import chylex.hee.game.mechanics.energy.IEnergyQuantity
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Units
import chylex.hee.system.util.FLAG_SKIP_RENDER
import chylex.hee.system.util.FLAG_SYNC_CLIENT
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.breakBlock
import chylex.hee.system.util.getTile

class TileEntityEndPortalAcceptor : TileEntityBasePortalController(){
	private companion object{
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
		get() = when(chargeState){
			IDLE, WAITING -> Invisible
			CHARGING      -> Animating(chargedEnergy.units.value.toFloat() / ENERGY_REQUIRED.units.value)
			FINISHED      -> Visible
		}
	
	override val clientAnimationFadeInSpeed
		get() = ANIMATION_PROGRESS_PER_UPDATE
	
	override val clientAnimationFadeOutSpeed
		get() = ANIMATION_PROGRESS_PER_UPDATE * 8F
	
	// Charge handling
	
	private enum class ChargeState(val refreshRate: Int){
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
	
	fun refreshClusterState(){
		if (world.isRemote || isRefreshing){
			return
		}
		
		isRefreshing = true // required to ignore a neighborChanged call when breaking the Cluster
		
		val newChargeState: ChargeState
		
		val posAbove = pos.up()
		val cluster = posAbove.getTile<TileEntityEnergyCluster>(world)
		
		if (cluster == null){
			newChargeState = when(chargeState){
				FINISHED -> FINISHED
				IDLE     -> IDLE
				WAITING  -> IDLE
				
				CHARGING -> {
					BlockEnergyCluster.createSmallLeak(world, posAbove, chargedEnergy)
					chargedEnergy = Units(0)
					IDLE
				}
			}
		}
		else{
			newChargeState = when(chargeState){
				FINISHED -> FINISHED
				IDLE     -> WAITING
				
				WAITING  ->
					if (cluster.energyLevel >= ENERGY_REQUIRED)
						CHARGING
					else
						WAITING
				
				CHARGING ->
					if (cluster.drainEnergy(ENERGY_PER_UPDATE)){
						chargedEnergy += ENERGY_PER_UPDATE
						
						if (chargedEnergy >= ENERGY_REQUIRED){
							cluster.breakWithoutExplosion = true
							cluster.pos.breakBlock(world, false)
							FINISHED
						}
						else{
							markDirty()
							CHARGING
						}
					}
					else{
						cluster.breakWithoutExplosion = true
						cluster.pos.breakBlock(world, false)
						CHARGING
					}
			}
		}
		
		if (chargeState != newChargeState){
			chargeState = newChargeState
			notifyUpdate(FLAG_SYNC_CLIENT or FLAG_SKIP_RENDER)
			markDirty()
		}
		
		isRefreshing = false
		ticksToRefresh = chargeState.refreshRate
	}
	
	// Overrides
	
	override fun firstTick(){
		refreshClusterState()
	}
	
	override fun update(){
		super.update()
		
		if (!world.isRemote && ticksToRefresh != NO_REFRESH && --ticksToRefresh <= 0){
			refreshClusterState()
		}
	}
	
	override fun writeNBT(nbt: TagCompound, context: Context) = with(nbt){
		super.writeNBT(nbt, context)
		
		if (context == STORAGE){
			setInteger(ENERGY_CHARGE_TAG, chargedEnergy.units.value)
		}
	}
	
	override fun readNBT(nbt: TagCompound, context: Context) = with(nbt){
		super.readNBT(nbt, context)
		
		if (context == STORAGE){
			chargedEnergy = Units(getInteger(ENERGY_CHARGE_TAG))
			
			if (chargedEnergy >= ENERGY_REQUIRED){
				chargeState = FINISHED
			}
			else if (chargedEnergy > Units(0)){
				chargeState = CHARGING
			}
		}
	}
}
