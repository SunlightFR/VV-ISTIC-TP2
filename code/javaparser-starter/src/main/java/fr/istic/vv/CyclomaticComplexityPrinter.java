package fr.istic.vv;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorWithDefaults;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CyclomaticComplexityPrinter extends VoidVisitorWithDefaults<Void> {

    private PrintStream stream;

    private int cc = 0;

    public CyclomaticComplexityPrinter(String filename) throws IOException {
        stream = new PrintStream(filename);
    }


    @Override
    public void visit(CompilationUnit unit, Void arg) {
        Optional<PackageDeclaration> o = unit.getPackageDeclaration();
        String packageName;
        if(o.isPresent()){
            packageName = o.get().getNameAsString();
        }else{
            packageName = "";
        }

        for(TypeDeclaration<?> type : unit.getTypes()) {
            type.accept(this, null);
            stream.println("# "+packageName+"."+type.getNameAsString());
            visitTypeDeclaration(type, null);
        }
    }

    public void visitTypeDeclaration(TypeDeclaration<?> declaration, Void arg) {

        if(!declaration.isPublic()) return;

        for(MethodDeclaration method : declaration.getMethods()) {
            method.accept(this, arg);
        }

        for(FieldDeclaration fieldDeclaration : declaration.getFields()) {
            fieldDeclaration.accept(this, arg);
        }

        for (BodyDeclaration<?> member : declaration.getMembers()) {
            if (member instanceof TypeDeclaration)
                member.accept(this, arg);
        }
    }


    @Override
    public void visit(MethodDeclaration declaration, Void arg) {
        cc = 0;
        if(!declaration.isPublic()) return;

        NodeList<Statement> s = declaration.getBody().get().getStatements();
        for(Statement ss:s){

            ss.accept(this, null);
        }
        stream.println("- "+declaration.getNameAsString()+": "+cc);

    }

    @Override
    public void visit(IfStmt s, Void arg){
        cc++;
        for(Statement ss: s.getThenStmt().asBlockStmt().getStatements()){
            ss.accept(this, arg);
        }
        if(s.getElseStmt().isPresent()){
            cc++;
            for(Statement ss:s.getElseStmt().get().asBlockStmt().getStatements()){
                ss.accept(this, arg);
            }
        }
    }

    @Override
    public void visit(ForStmt s, Void arg){
        cc++;
        for(Statement ss:s.getBody().asBlockStmt().getStatements()){
            ss.accept(this, arg);
        }
    }

    @Override
    public void visit(SwitchEntry s, Void arg){
        cc++;
    }

    @Override
    public void visit(WhileStmt s, Void arg){
        cc++;
        for(Statement ss:s.getBody().asBlockStmt().getStatements()){
            ss.accept(this, arg);
        }
    }

    @Override
    public void visit(DoStmt s, Void arg){
        cc++;
        for(Statement ss:s.getBody().asBlockStmt().getStatements()){
            ss.accept(this, arg);
        }
    }
}
