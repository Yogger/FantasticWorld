package com.mmorpg.mir.model.core.condition;

import com.mmorpg.mir.model.common.exception.ManagedErrorCode;
import com.mmorpg.mir.model.common.exception.ManagedException;
import com.mmorpg.mir.model.copy.manager.CopyManager;
import com.mmorpg.mir.model.copy.model.CopyType;
import com.mmorpg.mir.model.copy.resource.CopyResource;
import com.mmorpg.mir.model.core.condition.model.CoreConditionResource;
import com.mmorpg.mir.model.gameobjects.Player;
import com.mmorpg.mir.model.quest.model.Quest;

/**
 * 副本完成次数
 * 
 * @author Kuang Hao
 * @since v1.0 2014-8-21
 * 
 */
public class CopyNotCompleteCountCondition extends AbstractCoreCondition {
	
	private Operator op;

	@Override
	public boolean verify(Object object) {
		Player player = null;
		if (object instanceof Player) {
			player = (Player) object;
		}
		if (object instanceof Quest) {
			player = ((Quest) object).getOwner();
		}
		if (player == null) {
			this.errorObject(object);
		}
		int count = 0;
		CopyResource resource = CopyManager.getInstance().getCopyResources().get(code, true);
		if (resource.getType() == CopyType.LADDER) {
			if (resource.getIndex() <= player.getCopyHistory().getLadderCompleteIndex()) {
				count = 1;
			}
		} else {
			if (player.getCopyHistory().getCompleteHistory().containsKey(code)) {
				count += player.getCopyHistory().getCompleteHistory().get(code);
			}
		}
		if (op == Operator.GREATER_EQUAL) {
			return count >= value;
		} else if (op == Operator.GREATER) {
			return count > value;
		} else if (op == Operator.EQUAL) {
			return count == value;
		} else if (op == Operator.LESS_EQUAL) {
			return count <= value;
		} else if (op == Operator.LESS) {
			return count < value;
		}
		throw new ManagedException(ManagedErrorCode.COPY_NOT_COMPLETE);
	}
	
	@Override
	protected void init(CoreConditionResource resource) {
		super.init(resource);
		if (resource.getOperator() == null || resource.getOperator().isEmpty()) {
			this.op = Operator.LESS;
		} else {
			this.op = resource.getOperatorEnum();
		}
	}
}