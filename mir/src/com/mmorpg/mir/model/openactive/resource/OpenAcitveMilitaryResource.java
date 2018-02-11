package com.mmorpg.mir.model.openactive.resource;

import javax.persistence.Transient;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.mmorpg.mir.model.core.action.CoreActionManager;
import com.mmorpg.mir.model.core.action.CoreActions;
import com.mmorpg.mir.model.core.condition.CoreConditionManager;
import com.mmorpg.mir.model.core.condition.CoreConditions;
import com.windforce.common.resource.anno.Id;
import com.windforce.common.resource.anno.Resource;

@Resource
public class OpenAcitveMilitaryResource {
	@Id
	private String id;
	/** 领取的条件 */
	private String[] conditonIds;
	/** 领取消耗 */
	private String[] actionIds;
	/** 奖励 */
	private String rewardChooserGroupId;
	/** 服务器限量的资源ID */
	private String limitId;

	@Transient
	private CoreConditions coreConditions;

	@Transient
	private CoreActions coreActions;

	@JsonIgnore
	public CoreConditions getCoreConditions() {
		if (coreConditions == null) {
			coreConditions = CoreConditionManager.getInstance().getCoreConditions(1, conditonIds);
		}
		return coreConditions;
	}

	@JsonIgnore
	public void setCoreConditions(CoreConditions coreConditions) {
		this.coreConditions = coreConditions;
	}

	@JsonIgnore
	public CoreActions getCoreActions() {
		if (coreActions == null) {
			coreActions = CoreActionManager.getInstance().getCoreActions(1, actionIds);
		}
		return coreActions;
	}

	@JsonIgnore
	public void setCoreActions(CoreActions coreActions) {
		this.coreActions = coreActions;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String[] getConditonIds() {
		return conditonIds;
	}

	public void setConditonIds(String[] conditonIds) {
		this.conditonIds = conditonIds;
	}

	public String getRewardChooserGroupId() {
		return rewardChooserGroupId;
	}

	public void setRewardChooserGroupId(String rewardChooserGroupId) {
		this.rewardChooserGroupId = rewardChooserGroupId;
	}

	public String[] getActionIds() {
		return actionIds;
	}

	public void setActionIds(String[] actionIds) {
		this.actionIds = actionIds;
	}

	public String getLimitId() {
		return limitId;
	}

	public void setLimitId(String limitId) {
		this.limitId = limitId;
	}
	
}