package org.d2rq.db.expr;

import org.d2rq.db.types.DataType.GenericType;



public class LessThan extends BinaryOperator {

	public LessThan(Expression expr1, Expression expr2) {
		super(expr1, expr2, "<", false, GenericType.BOOLEAN);
	}

	@Override
	protected Expression clone(Expression newOperand1, Expression newOperand2) {
		return new LessThan(newOperand1, newOperand2);
	}
}
