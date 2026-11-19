package ru.nilsson03.library.quest.util.sql;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/** SQL storage can use the caller's DataSource or the legacy Database-core registry. */
public final class QuestConnections {
    private QuestConnections() {}

    public static Connection open(Plugin owner, DataSource source) throws SQLException {
        if (source != null) return source.getConnection();

        Plugin database = Bukkit.getPluginManager().getPlugin("Database-core");
        if (database != null && database.isEnabled()) {
            try {
                Class<?> registry = Class.forName("ru.nilsson03.hikaricp.SharedPoolRegistry", true,
                        database.getClass().getClassLoader());
                for (var method : registry.getMethods()) {
                    if (method.getName().equals("getConnection") && method.getParameterCount() == 1
                            && java.lang.reflect.Modifier.isStatic(method.getModifiers())
                            && method.getParameterTypes()[0].isInstance(owner)) {
                        return (Connection) method.invoke(null, owner);
                    }
                }
            } catch (InvocationTargetException failure) {
                if (failure.getCause() instanceof SQLException sql) throw sql;
                throw new SQLException("Could not open the Database-core pool for " + owner.getName(), failure.getCause());
            } catch (ReflectiveOperationException | LinkageError failure) {
                throw new SQLException("Could not use Database-core for " + owner.getName(), failure);
            }
        }
        throw new SQLException("No SQL DataSource for " + owner.getName()
                + ". Supply a DataSource or select file storage explicitly.");
    }
}
