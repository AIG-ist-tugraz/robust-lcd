/*
 * Genetic Conflict Seeker
 *
 * Copyright (c) 2022-2023
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.conflict.kb.apm;

import at.tugraz.ist.ase.hiconfit.common.LoggerUtils;
import at.tugraz.ist.ase.hiconfit.kb.core.*;
import at.tugraz.ist.ase.hiconfit.kb.core.builder.IntVarConstraintBuilder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Air Pollution Monitoring (APM) Knowledge Base
 */
@Slf4j
public class ApmKB extends KB implements IIntVarKB {
    public ApmKB(boolean hasNegativeConstraints) {
        super("Air Pollution Monitoring Configuration Problem", "", hasNegativeConstraints);

        reset(hasNegativeConstraints);
    }

    @Override
    public void reset(boolean hasNegativeConstraints) {
        log.trace("{}Creating ApmKB >>>", LoggerUtils.tab());
        LoggerUtils.indent();

        modelKB = new Model(name);
        variableList = new LinkedList<>();
        domainList = new LinkedList<>();
        constraintList = new LinkedList<>();
        defineDomains();
        defineVariables();
        defineConstraints(hasNegativeConstraints);

        LoggerUtils.outdent();
        log.debug("{}<<< Created CameraKB", LoggerUtils.tab());
    }

    private void defineDomains() {
        log.trace("{}Defining domains >>>", LoggerUtils.tab());
        LoggerUtils.indent();

        domainList.add(Domain.builder() // 2 items
                .name("Communication_MS")
                .values(List.of("wired", "wireless"))
                .build());
        domainList.add(Domain.builder() // 2 items
                .name("Storage_MS")
                .values(List.of("local", "cloud"))
                .build());
        domainList.add(Domain.builder() // binary
                .name("Enclosure_MS")
                .values(List.of("rugged", "standard"))
                .build());
        domainList.add(Domain.builder() // binary
                .name("Type_DE")
                .values(List.of("indoor", "outdoor"))
                .build());
        domainList.add(Domain.builder() // binary
                .name("Context_DE")
                .values(List.of("country", "tropical"))
                .build());
        domainList.add(Domain.builder() // binary
                .name("Location_DE")
                .values(List.of("urban", "countryside", "industrial"))
                .build());
        domainList.add(Domain.builder()
                .name("Type_A")
                .values(List.of("indoor", "outdoor"))
                .build());
        domainList.add(Domain.builder()
                .name("Category_A")
                .values(List.of("industry", "shop", "library", "field"))
                .build());
        domainList.add(Domain.builder()
                .name("Traffic_A")
                .values(List.of("light", "medium", "heavy"))
                .build());
        domainList.add(Domain.builder()
                .name("Type_WT")
                .values(List.of("wood", "tiles", "plaster"))
                .build());

        // create a list of values from -50 to 50
        List<String> values = new ArrayList<>();
        List<String> finalValues = values;
        IntStream.range(-50, 51).forEachOrdered(i -> finalValues.add(String.valueOf(i)));
        domainList.add(Domain.builder()
                .name("AvgTemp_EC")
                .values(values)
                .build());

        // create a list of values from 0 to 17
        values = new ArrayList<>();
        List<String> finalValues1 = values;
        IntStream.range(0, 18).forEachOrdered(i -> finalValues1.add(String.valueOf(i)));
        domainList.add(Domain.builder()
                .name("AvgWind_EC")
                .values(values)
                .build());

        // create a list of values from 1 to 10
        values = new ArrayList<>();
        List<String> finalValues2 = values;
        IntStream.range(1, 11).forEachOrdered(i -> finalValues2.add(String.valueOf(i)));
        domainList.add(Domain.builder()
                .name("AvgPressure_EC")
                .values(values)
                .build());

        LoggerUtils.outdent();
        log.trace("{}<<< Created domains", LoggerUtils.tab());
    }

    public void defineVariables (){
        log.trace("{}Defining variables >>", LoggerUtils.tab());
        LoggerUtils.indent();

        List<String> varNames = List.of("Communication_MS", "Storage_MS", "Enclosure_MS", "Type_DE", "Context_DE",
                "Location_DE", "Type_A", "Category_A", "Traffic_A", "Type_WT", "AvgTemp_EC", "AvgWind_EC", "AvgPressure_EC");

        IntStream.range(0, varNames.size()).forEachOrdered(i -> {
            String varName = varNames.get(i);
            IntVar intVar = this.modelKB.intVar(varName, domainList.get(i).getIntValues());
            Variable var = IntVariable.builder()
                    .name(varName)
                    .domain(domainList.get(i))
                    .chocoVar(intVar).build();
            variableList.add(var);
        });

        LoggerUtils.outdent();
        log.trace("{}<<< Created variables", LoggerUtils.tab());
    }

    public void defineConstraints(boolean hasNegativeConstraints) {
        log.trace("{}Defining constraints >>>", LoggerUtils.tab());
        LoggerUtils.indent();

        int startIdx = 0;

        // Cat_A = field --> Type_DE = outdoor
        // <=> not(Cat_A = field) or Type_DE = outdoor
        // <=> Cat_A != field or Type_DE = outdoor
//        model.ifThen( model.arithm(variables[7], "=", 4), model.arithm(variables[3], "=", 2) );
        startIdx = modelKB.getNbCstrs();
        org.chocosolver.solver.constraints.Constraint chocoConstraint = modelKB.or(modelKB.arithm(((IntVariable)variableList.get(7)).getChocoVar(), "!=", 3) , modelKB.arithm(((IntVariable)variableList.get(3)).getChocoVar(), "=", 1));
        Constraint constraint = IntVarConstraintBuilder.build("Cat_A = field --> Type_DE = outdoor", modelKB, chocoConstraint, startIdx, hasNegativeConstraints);
        constraintList.add(constraint);

        // Type_DE = indoor --> Communication_MS != wired
        // <=> not(Type_DE = indoor) or Communication_MS != wired
        // <=> Type_DE != indoor or Communication_MS != wired
//        model.ifThen( model.arithm(variables[3], "=", 2), model.arithm(variables[0], "!=", 1) );
        startIdx = modelKB.getNbCstrs();
        chocoConstraint = modelKB.or(modelKB.arithm(((IntVariable)variableList.get(3)).getChocoVar(), "!=", 0) , modelKB.arithm(((IntVariable)variableList.get(0)).getChocoVar(), "!=", 0));
        constraint = IntVarConstraintBuilder.build("Type_DE = indoor --> Communication_MS != wired", modelKB, chocoConstraint, startIdx, hasNegativeConstraints);
        constraintList.add(constraint);

        // Cat_A = library --> Type_DE = indoor
        // <=> not(Cat_A = library) or Type_DE = indoor
        // <=> Cat_A != library or Type_DE = indoor
//        model.ifThen( model.arithm(variables[7], "=", 3), model.arithm(variables[3], "=", 1) );
        startIdx = modelKB.getNbCstrs();
        chocoConstraint = modelKB.or(modelKB.arithm(((IntVariable)variableList.get(7)).getChocoVar(), "!=", 2) , modelKB.arithm(((IntVariable)variableList.get(3)).getChocoVar(), "=", 0));
        constraint = IntVarConstraintBuilder.build("Cat_A = library --> Type_DE = indoor", modelKB, chocoConstraint, startIdx, hasNegativeConstraints);
        constraintList.add(constraint);

        // Type_DE = indoor <--> Type_A = indoor
        // <=> (Type_DE = indoor --> Type_A = indoor) and (Type_A = indoor --> Type_DE = indoor)
        // <=> (not(Type_DE = indoor) or Type_A = indoor) and (not(Type_A = indoor) or Type_DE = indoor)
        // <=> (Type_DE != indoor or Type_A = indoor) and (Type_A != indoor or Type_DE = indoor)
//        model.ifThen( model.arithm(variables[3], "=", 1), model.arithm(variables[6], "=", 1) );
//        model.ifThen( model.arithm(variables[6], "=", 1), model.arithm(variables[3], "=", 1) );
        startIdx = modelKB.getNbCstrs();
        chocoConstraint = modelKB.and(
                modelKB.or(modelKB.arithm(((IntVariable)variableList.get(3)).getChocoVar(), "!=", 0) , modelKB.arithm(((IntVariable)variableList.get(6)).getChocoVar(), "=", 0)) ,
                modelKB.or(modelKB.arithm(((IntVariable)variableList.get(6)).getChocoVar(), "!=", 0) , modelKB.arithm(((IntVariable)variableList.get(3)).getChocoVar(), "=", 0)));
        constraint = IntVarConstraintBuilder.build("Type_DE = indoor <--> Type_A = indoor", modelKB, chocoConstraint, startIdx, hasNegativeConstraints);
        constraintList.add(constraint);

        // Category_A = field --> Traffic_A = light
        // <=> not(Category_A = field) or Traffic_A = light
        // <=> Category_A != field or Traffic_A = light
//        model.ifThen( model.arithm(variables[7], "=", 4), model.arithm(variables[8], "=", 1) );
        startIdx = modelKB.getNbCstrs();
        chocoConstraint = modelKB.or(modelKB.arithm(((IntVariable)variableList.get(7)).getChocoVar(), "!=", 3) , modelKB.arithm(((IntVariable)variableList.get(8)).getChocoVar(), "=", 0));
        constraint = IntVarConstraintBuilder.build("Category_A = field --> Traffic_A = light", modelKB, chocoConstraint, startIdx, hasNegativeConstraints);
        constraintList.add(constraint);

        // Type_DE = outdoor <--> AvgWind_EC != 0
        // <=> (Type_DE = outdoor --> AvgWind_EC != 0) and (AvgWind_EC != 0 --> Type_DE = outdoor)
        // <=> (not(Type_DE = outdoor) or AvgWind_EC != 0) and (not(AvgWind_EC != 0) or Type_DE = outdoor)
        // <=> (Type_DE != outdoor or AvgWind_EC != 0) and (AvgWind_EC = 0 or Type_DE = outdoor)
//        model.ifThen( model.arithm(variables[3], "=", 2), model.arithm(variables[11], "!=", 0) );
//        model.ifThen( model.arithm(variables[11], "!=", 0), model.arithm(variables[3], "=", 2) );
        startIdx = modelKB.getNbCstrs();
        chocoConstraint = modelKB.and(
                modelKB.or(modelKB.arithm(((IntVariable)variableList.get(3)).getChocoVar(), "!=", 1) , modelKB.arithm(((IntVariable)variableList.get(11)).getChocoVar(), "!=", 0)) ,
                modelKB.or(modelKB.arithm(((IntVariable)variableList.get(11)).getChocoVar(), "=", 0) , modelKB.arithm(((IntVariable)variableList.get(3)).getChocoVar(), "=", 1)));
        constraint = IntVarConstraintBuilder.build("Type_DE = outdoor <--> AvgWind_EC != 0", modelKB, chocoConstraint, startIdx, hasNegativeConstraints);
        constraintList.add(constraint);

        LoggerUtils.outdent();
        log.trace("{}<<< Created constraints", LoggerUtils.tab());
    }

    @Override
    public IntVar[] getIntVars() {
        org.chocosolver.solver.variables.Variable[] vars = getModelKB().getVars();

        return Arrays.stream(vars).map(v -> (IntVar) v).toArray(IntVar[]::new);
    }

    @Override
    public IntVar getIntVar(@NonNull String variable) {
        Variable var = getVariable(variable);

        return ((IntVariable) var).getChocoVar();
    }

    // Choco value
    @Override
    public int getIntValue(@NonNull String var, @NonNull String value) {
        Domain domain = getDomain(var);

        return domain.getChocoValue(value);
    }
}
