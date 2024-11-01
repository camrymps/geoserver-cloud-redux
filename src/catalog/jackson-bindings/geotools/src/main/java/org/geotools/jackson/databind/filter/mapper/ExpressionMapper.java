/*
 * (c) 2020 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geotools.jackson.databind.filter.mapper;

import lombok.Generated;

import org.geotools.api.filter.expression.ExpressionVisitor;
import org.geotools.api.filter.expression.NilExpression;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.FunctionFinder;
import org.geotools.filter.capability.FunctionNameImpl;
import org.geotools.jackson.databind.filter.dto.Expression;
import org.geotools.jackson.databind.filter.dto.Expression.Add;
import org.geotools.jackson.databind.filter.dto.Expression.Divide;
import org.geotools.jackson.databind.filter.dto.Expression.Function;
import org.geotools.jackson.databind.filter.dto.Expression.Multiply;
import org.geotools.jackson.databind.filter.dto.Expression.PropertyName;
import org.geotools.jackson.databind.filter.dto.Expression.Subtract;
import org.geotools.jackson.databind.filter.dto.Literal;
import org.mapstruct.AnnotateWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ObjectFactory;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "default",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {ExpressionFactory.class, FilterFactory.class, GeoToolsValueMappers.class})
@AnnotateWith(value = Generated.class)
public abstract class ExpressionMapper {

    private final org.geotools.api.filter.FilterFactory ff = CommonFactoryFinder.getFilterFactory();

    private final ExpressionVisitor visitor =
            new ExpressionVisitor() {

                @Override
                public Subtract visit(
                        org.geotools.api.filter.expression.Subtract expression, Object extraData) {
                    return map(expression);
                }

                @Override
                public PropertyName visit(
                        org.geotools.api.filter.expression.PropertyName expression,
                        Object extraData) {
                    return map(expression);
                }

                @Override
                public Multiply visit(
                        org.geotools.api.filter.expression.Multiply expression, Object extraData) {
                    return map(expression);
                }

                @Override
                public Literal visit(
                        org.geotools.api.filter.expression.Literal expression, Object extraData) {
                    return map(expression);
                }

                @Override
                public Function visit(
                        org.geotools.api.filter.expression.Function expression, Object extraData) {
                    return map(expression);
                }

                @Override
                public Divide visit(
                        org.geotools.api.filter.expression.Divide expression, Object extraData) {
                    return map(expression);
                }

                @Override
                public Add visit(
                        org.geotools.api.filter.expression.Add expression, Object extraData) {
                    return map(expression);
                }

                @Override
                public Expression visit(NilExpression expression, Object extraData) {
                    return map(expression);
                }
            };

    public Expression map(org.geotools.api.filter.expression.Expression source) {
        return (Expression) source.accept(visitor, null);
    }

    public org.geotools.api.filter.expression.Expression map(Expression source) {
        if (source == null) return null;
        if (source instanceof Literal literal) return map(literal);
        if (source instanceof PropertyName prop) return map(prop);
        if (source instanceof Add add) return map(add);
        if (source instanceof Subtract subtract) return map(subtract);
        if (source instanceof Multiply multiply) return map(multiply);
        if (source instanceof Divide divide) return map(divide);
        if (source instanceof Function function) return map(function);
        throw new IllegalArgumentException(
                "Unrecognized expression type %s: %s"
                        .formatted(source.getClass().getName(), source));
    }

    public @ObjectFactory org.geotools.api.filter.capability.FunctionName functionName(
            Expression.FunctionName dto) {
        FunctionFinder finder = new FunctionFinder(null);
        String functionName = dto.getName();
        org.geotools.api.filter.capability.FunctionName name =
                finder.findFunctionDescription(functionName);
        if (name == null) {
            int argumentCount = dto.getArgumentCount();
            List<String> argumentNames = dto.getArgumentNames();
            if (argumentNames != null) {
                name = new FunctionNameImpl(functionName, argumentCount, argumentNames);
            } else {
                name = new FunctionNameImpl(functionName, argumentCount);
            }
        }
        return name;
    }

    public org.geotools.api.filter.capability.FunctionName map(Expression.FunctionName dto) {
        return functionName(dto);
    }

    public Expression.FunctionName map(org.geotools.api.filter.capability.FunctionName value) {
        Expression.FunctionName dto = new Expression.FunctionName();
        dto.setName(value.getName())
                .setArgumentCount(value.getArgumentCount())
                .setArgumentNames(value.getArgumentNames());
        return dto;
    }

    public abstract PropertyName map(org.geotools.api.filter.expression.PropertyName expression);

    public abstract org.geotools.api.filter.expression.PropertyName map(PropertyName dto);

    public abstract org.geotools.api.filter.expression.Literal map(Literal dto);

    public abstract Literal map(org.geotools.api.filter.expression.Literal expression);

    protected org.geotools.api.filter.expression.Function map(Function dto) {
        if (dto == null) return null;
        org.geotools.api.filter.expression.Expression[] parameters;
        parameters = dtoListToExpressionList(dto.getParameters());
        if (parameters == null) parameters = new org.geotools.api.filter.expression.Expression[0];
        return ff.function(dto.getName(), parameters);
    }

    protected abstract org.geotools.api.filter.expression.Expression[] dtoListToExpressionList(
            List<Expression> list);

    protected abstract Expression.Function map(
            org.geotools.api.filter.expression.Function expression);

    protected abstract Add map(org.geotools.api.filter.expression.Add expression);

    @Mapping(target = "expr1", source = "expression1")
    @Mapping(target = "expr2", source = "expression2")
    @Mapping(target = "expression1", ignore = true)
    @Mapping(target = "expression2", ignore = true)
    protected abstract org.geotools.filter.expression.AddImpl map(Add dto);

    protected abstract Subtract map(org.geotools.api.filter.expression.Subtract expression);

    @Mapping(target = "expr1", source = "expression1")
    @Mapping(target = "expr2", source = "expression2")
    @Mapping(target = "expression1", ignore = true)
    @Mapping(target = "expression2", ignore = true)
    protected abstract org.geotools.filter.expression.SubtractImpl map(Subtract dto);

    protected abstract Divide map(org.geotools.api.filter.expression.Divide expression);

    @Mapping(target = "expr1", source = "expression1")
    @Mapping(target = "expr2", source = "expression2")
    @Mapping(target = "expression1", ignore = true)
    @Mapping(target = "expression2", ignore = true)
    protected abstract org.geotools.filter.expression.DivideImpl map(Divide dto);

    protected abstract Multiply map(org.geotools.api.filter.expression.Multiply expression);

    @Mapping(target = "expr1", source = "expression1")
    @Mapping(target = "expr2", source = "expression2")
    @Mapping(target = "expression1", ignore = true)
    @Mapping(target = "expression2", ignore = true)
    protected abstract org.geotools.filter.expression.MultiplyImpl map(Multiply dto);
}
