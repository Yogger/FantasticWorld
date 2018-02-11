package com.mmorpg.mir.model.agateinvest;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.mmorpg.mir.model.agateinvest.model.InvestAgateType;
import com.mmorpg.mir.model.agateinvest.resource.InvestAgateResource;
import com.mmorpg.mir.model.common.ConfigValue;
import com.mmorpg.mir.model.core.condition.CoreConditionManager;
import com.mmorpg.mir.model.core.condition.CoreConditions;
import com.windforce.common.resource.Storage;
import com.windforce.common.resource.anno.Static;

@Component
public class InvestAgateConfig {

	private static InvestAgateConfig instance;

	@PostConstruct
	void init() {
		instance = this;
	}

	public static InvestAgateConfig getInstance() {
		return instance;
	}

	@Static
	public Storage<String, InvestAgateResource> investStorage;

	/** 各种投资的购买消耗 */
	@Static("INVESTCORNUCOPIA:BUY_COST")
	private ConfigValue<Map<String, String>> BUY_COST;

	public String getBuyCostByType(InvestAgateType type) {
		return BUY_COST.getValue().get(type.getType() + "");
	}

	/** 经验投资购买条件 */
	@Static("INVESTCORNUCOPIA:EXP_BUY_CONDS")
	private ConfigValue<String[]> EXP_BUY_CONDS;

	/** 铜钱投资购买条件 */
	@Static("INVESTCORNUCOPIA:COPPER_BUY_CONDS")
	private ConfigValue<String[]> COPPER_BUY_CONDS;

	/** 复活丹投资购买条件 */
//	@Static("INVESTCORNUCOPIA:RELIVEDAN_BUY_CONDS")
//	private ConfigValue<String[]> RELIVEDAN_BUY_CONDS;

	/** 荣誉投资购买条件 */
	@Static("INVESTCORNUCOPIA:HONOR_BUY_CONDS")
	private ConfigValue<String[]> HONOR_BUY_CONDS;

	/** 礼金投资购买条件 */
	@Static("INVESTCORNUCOPIA:GIFT_BUY_CONDS")
	private ConfigValue<String[]> GIFT_BUY_CONDS;

	public CoreConditions getBuyConditions(InvestAgateType type) {
		CoreConditions coreConditions = null;
		switch (type) {
		case EXP: {
			coreConditions = CoreConditionManager.getInstance().getCoreConditions(1, EXP_BUY_CONDS.getValue());
			break;
		}
		case COPPER: {
			coreConditions = CoreConditionManager.getInstance().getCoreConditions(1, COPPER_BUY_CONDS.getValue());
			break;
		}
//		case RELIVEDAN: {
//			coreConditions = CoreConditionManager.getInstance().getCoreConditions(1, RELIVEDAN_BUY_CONDS.getValue());
//			break;
//		}
		case HONOR: {
			coreConditions = CoreConditionManager.getInstance().getCoreConditions(1, HONOR_BUY_CONDS.getValue());
			break;
		}
		case GIFT: {
			coreConditions = CoreConditionManager.getInstance().getCoreConditions(1, GIFT_BUY_CONDS.getValue());
			break;
		}
		}
		return coreConditions;
	}

	@Static("INVESTCORNUCOPIA:HISTORY_RECORD_MAX_COUN")
	public ConfigValue<Integer> HISTORY_RECORD_MAX_COUNT;

}