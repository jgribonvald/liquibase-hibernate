package liquibase.ext.hibernate.snapshot;

import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.statement.DatabaseFunction;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Index;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Table;

import org.hibernate.dialect.PostgreSQL81Dialect;
import org.hibernate.id.IdentityGenerator;
import org.hibernate.mapping.SimpleValue;

import java.util.ArrayList;
import java.util.List;

public class PrimaryKeySnapshotGenerator extends HibernateSnapshotGenerator {

    public PrimaryKeySnapshotGenerator() {
        super(PrimaryKey.class, new Class[]{Table.class});
    }

    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        return example;
    }

    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        if (!snapshot.getSnapshotControl().shouldInclude(PrimaryKey.class)) {
            return;
        }
        if (foundObject instanceof Table) {
            Table table = (Table) foundObject;
            org.hibernate.mapping.Table hibernateTable = findHibernateTable(table, snapshot);
            if (hibernateTable == null) {
                return;
            }
            org.hibernate.mapping.PrimaryKey hibernatePrimaryKey = hibernateTable.getPrimaryKey();
            if (hibernatePrimaryKey != null) {
                PrimaryKey pk = new PrimaryKey();
                pk.setName(hibernatePrimaryKey.getName());
                pk.setTable(table);
                for (Object hibernateColumn : hibernatePrimaryKey.getColumns()) {
                    pk.getColumns().add(new Column(((org.hibernate.mapping.Column) hibernateColumn).getName()).setRelation(table));
                }

                LOG.info("Found primary key " + pk.getName());
                table.setPrimaryKey(pk);
                Index index = new Index();
                index.setName("IX_" + pk.getName());
                index.setTable(table);
                index.setColumns(pk.getColumns());
                index.setUnique(true);
                pk.setBackingIndex(index);
                table.getIndexes().add(index);
            }
        }
    }

}