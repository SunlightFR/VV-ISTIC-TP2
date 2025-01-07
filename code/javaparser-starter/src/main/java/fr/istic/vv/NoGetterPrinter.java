package fr.istic.vv;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.VoidVisitorWithDefaults;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


// This class visits a compilation unit and
// prints all public enum, classes or interfaces along with their public methods
public class NoGetterPrinter extends VoidVisitorWithDefaults<Void> {

    private List<String> variables = new ArrayList<>();
    private List<String> getters = new ArrayList<>();
    private String className;
    private String packageName;

    private PrintStream stream;

    public NoGetterPrinter(String filename) throws FileNotFoundException {
        this.stream = new PrintStream(filename);
    }

    @Override
    public void visit(CompilationUnit unit, Void arg) {
        Optional<PackageDeclaration> o = unit.getPackageDeclaration();
        if(o.isPresent()){
            packageName = o.get().getNameAsString();
        }else{
            packageName = "";
        }
        for(TypeDeclaration<?> type : unit.getTypes()) {
            type.accept(this, null);
            visitTypeDeclaration(type, null);
        }
        stream.println("# "+packageName+"."+className);
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


        for(String g : getters){
            variables.removeIf(v -> g.equalsIgnoreCase("get" + v));
        }

        for(String v:variables){
            stream.println("- "+v);
        }


        getters.clear();
        variables.clear();

    }




    @Override
    public void visit(ClassOrInterfaceDeclaration declaration, Void arg) {
        className = declaration.getNameAsString();
    }

    @Override
    public void visit(FieldDeclaration n, Void arg) {
        super.visit(n, arg);
        if(n.isPublic()) return;
//        if(n.isStatic()) return;
        for(VariableDeclarator v : n.getVariables()){
            variables.add(v.getNameAsString());
        }

    }

    @Override
    public void visit(MethodDeclaration declaration, Void arg) {
        if(!declaration.isPublic()) return;
        if(declaration.getNameAsString().startsWith("get")){
            if(declaration.getBody().isPresent()){
                NodeList<Statement> ss = declaration.getBody().get().getStatements();
                if(ss.size()==1 && ss.get(0).isReturnStmt()){
                    getters.add(declaration.getNameAsString());
                }
            }
        }

    }

}
