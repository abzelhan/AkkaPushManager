package util;

import com.zaxxer.hikari.HikariDataSource;
import model.Push;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * Created by abzalsahitov@gmail.com  on 3/29/18.
 */
public class DBUtil {

    private static HikariDataSource hikariDataSource;
    public static final String HOST_NAME = "root";
    public static final String HOST_PASSWORD = "root";//root12
    public static final String HOST_DATABASE = "autovse_pusher";

    static {

        hikariDataSource = new HikariDataSource();
        hikariDataSource.setJdbcUrl("jdbc:mysql://localhost:3306/" + HOST_DATABASE + "?useUnicode=true&characterEncoding=utf8");
        hikariDataSource.setUsername(HOST_NAME);
        hikariDataSource.setPassword(HOST_PASSWORD);
        System.out.println("Database connection pool configured and started. ");
    }

    public static HikariDataSource getHikariDataSource() {
        return hikariDataSource;
    }

    public static void setHikariDataSource(HikariDataSource hikariDataSource) {
        DBUtil.hikariDataSource = hikariDataSource;
    }

    public static Connection getConnection() throws SQLException {
        return hikariDataSource.getConnection();
    }

    public static List<String> getTokens(Long user_id, String source, boolean isAdmin) throws Exception {
        List<String> tokens = new ArrayList<String>();
        try (Connection con = getConnection()) {
            PreparedStatement ps = con.prepareStatement("select DISTINCT token from deviceTokens " +
                    " where user_id=? " +
                    " and source=? " +
                    " and isAdmin=? " +
                    " and deleted=0 " +
                    " and state=0 " +
                    " and token<>'' ");
            ps.setLong(1, user_id);
            ps.setString(2, source);
            ps.setBoolean(3, isAdmin);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                tokens.add(rs.getString(1));
            }
            if (tokens.isEmpty()) {
                ps = con.prepareStatement("select DISTINCT token from deviceTokens " +
                        " where user_id=? " +
                        " and source=? " +
                        " and isAdmin=? " +
                        " and deleted=0 " +
                        " and token<>'' ");

                ps.setLong(1, user_id);
                ps.setString(2, source);
                ps.setBoolean(3, isAdmin);

                rs = ps.executeQuery();

                while (rs.next()) {
                    tokens.add(rs.getString(1));
                }
            }
            rs.close();
            ps.close();
        }
        return tokens;
    }

    public static long getAvailablePushesCount() throws SQLException {
        long totalAvailable = 0;
        try (Connection connection = getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement("select count(id) as amount from push")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        totalAvailable = rs.getLong("amount");
                    }
                }
            }
        }
        return totalAvailable;
    }

    public static void setFetched(long id) throws SQLException {
        try (Connection connection = getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement("update push set attempts=1 where id=?")) {
                ps.setLong(1, id);
                ps.executeUpdate();
            }
        }
    }

    public static void setSended(long id) throws SQLException {
        try (Connection connection = getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement("update push set state=1 where id=?")) {
                ps.setLong(1, id);
                ps.executeUpdate();
            }
        }
    }

    public static List<Push> getAvailablePushesList(int amount) throws SQLException {
        List<Push> pushes = new ArrayList<>();
        try (Connection connection = getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement("select * from push where state=0 and attempts=0 limit ?")) {
                ps.setInt(1, amount);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Push push = new Push();
                        push.setId(rs.getLong("id"));
                        push.setAdmin(rs.getBoolean("admin"));
                        push.setTitle(rs.getString("title"));
                        push.setAttempts(rs.getInt("attempts"));
                        push.setUser_id(rs.getLong("user_id"));
                        push.setBody(rs.getString("text"));
                        pushes.add(push);
                    }
                }
            }
        }
        pushes.forEach(p -> {
            try {
                setFetched(p.getId());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        return pushes;
    }


    public static List<Push> getAvailablePushesListAndMarkFetched(int amount) throws SQLException {
        List<Push> pushes = new ArrayList<>();
        try (Connection connection = getConnection()) {
            PreparedStatement selectPs = connection.prepareStatement("select * from push where state=0 and attempts=0 limit ?");
            PreparedStatement insertPs = connection.prepareStatement("update push set attempts=1 where id=?");
            selectPs.setInt(1, amount);
            try (ResultSet rs = selectPs.executeQuery()) {
                while (rs.next()) {
                    Push push = new Push();
                    push.setId(rs.getLong("id"));
                    if(rs.getTimestamp("sent")!=null){
                        Calendar sent = Calendar.getInstance();
                        sent.setTimeInMillis(rs.getTimestamp("sent").getTime());
                        push.setSent(sent);
                    }
                    if(rs.getTimestamp("created")!=null){
                        Calendar created = Calendar.getInstance();
                        created.setTimeInMillis(rs.getTimestamp("created").getTime());
                        push.setCreated(created);
                    }
                    push.setAdmin(rs.getBoolean("admin"));
                    push.setTitle(rs.getString("title"));
                    push.setAttempts(rs.getInt("attempts"));
                    push.setUser_id(rs.getLong("user_id"));
                    push.setDeviceToken_id(rs.getLong("deviceToken_id"));
                    push.setTimestamp(rs.getLong("timestamp"));
                    push.setDelayType(rs.getInt("delayType"));
                    push.setBody(rs.getString("text"));
                    push.setSubtitle(rs.getString("subtitle"));
                    push.setS(rs.getInt("s"));
                    push.setS2(rs.getInt("s2"));
                    push.setBadge(rs.getInt("badge"));
                    push.setActionKey(rs.getString("actionKey"));
                    pushes.add(push);

                    //select all push arguments
                    try(PreparedStatement selectMapPs = connection.prepareStatement("select * from Push_arguments where Push_id=?")){
                    selectMapPs.setLong(1,push.getId());
                    try(ResultSet resultSet = selectMapPs.executeQuery()){
                        HashMap<String, String> pushArguments = new HashMap<>();
                        while (resultSet.next()){
                            String arguments = resultSet.getString("arguments");
                            String arguments_key = resultSet.getString("arguments_KEY");
                            pushArguments.put(arguments_key,arguments);
                        }
                        push.setArguments(pushArguments);
                    }
                    }


                    //for big batch
                    insertPs.setLong(1, push.getId());
                    insertPs.addBatch();
                }
            }
            insertPs.executeBatch();
            insertPs.close();
            selectPs.close();
        }
        return pushes;
    }

    public static void blockDeviceToken(String token,String blockedReason) throws SQLException {
        try (Connection connection = getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement("update deviceTokens set state=1,blockedReason=? where token=?")) {
                ps.setString(1, blockedReason);
                ps.setString(2,token);
                ps.executeUpdate();
            }
        }
    }

}

/*
SQL Migration code

CREATE TABLE `deviceTokens` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creationDate` datetime DEFAULT NULL,
  `deleted` int(11) DEFAULT '0',
  `lastModificationDate` datetime DEFAULT NULL,
  `orderNum` int(11) DEFAULT '0',
  `state` int(11) DEFAULT '0',
  `uuid` varchar(255) DEFAULT NULL,
  `created` datetime DEFAULT NULL,
  `source` varchar(255) DEFAULT NULL,
  `token` varchar(255) DEFAULT NULL,
  `updated` datetime DEFAULT NULL,
  `creator_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `isAdmin` tinyint(1) DEFAULT '0',
  `blockedReason` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKCA19F79047140EFE` (`user_id`),
  KEY `FKCA19F790A21012FD` (`creator_id`)
) ENGINE=InnoDB AUTO_INCREMENT=40265 DEFAULT CHARSET=utf8


CREATE TABLE `push` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creationDate` datetime DEFAULT NULL,
  `deleted` int(11) DEFAULT '0',
  `lastModificationDate` datetime DEFAULT NULL,
  `orderNum` int(11) DEFAULT '0',
  `state` int(11) DEFAULT '0',
  `uuid` varchar(255) DEFAULT NULL,
  `actionKey` varchar(255) DEFAULT NULL,
  `attempts` int(11) DEFAULT '0',
  `badge` int(11) DEFAULT '0',
  `created` datetime DEFAULT NULL,
  `s` int(11) DEFAULT '0',
  `sent` datetime DEFAULT NULL,
  `text` longtext,
  `timestamp` bigint(20) DEFAULT '0',
  `title` text,
  `url` varchar(255) DEFAULT NULL,
  `creator_id` bigint(20) DEFAULT NULL,
  `deviceToken_id` bigint(20) DEFAULT NULL,
  `admin` tinyint(4) DEFAULT '0',
  `user_id` bigint(20) DEFAULT NULL,
  `subtitle` text,
  `toServiceCompany_id` bigint(20) DEFAULT NULL,
  `toUser_id` bigint(20) DEFAULT NULL,
  `toServiceCompanyAdmins_id` bigint(20) DEFAULT NULL,
  `s2` int(11) DEFAULT '0',
  `delayType` int(11) DEFAULT '0',
  `sentDevices` int(11) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `FK34AF1ABB1E5D16` (`deviceToken_id`),
  KEY `FK34AF1AA21012FD` (`creator_id`),
  KEY `FK34AF1A47140EFE` (`user_id`),
  KEY `FK34AF1A703EA866` (`toServiceCompany_id`),
  KEY `FK34AF1A80B8D023` (`toUser_id`),
  KEY `FK34AF1AED08B05F` (`toServiceCompanyAdmins_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2854842 DEFAULT CHARSET=utf8

CREATE TABLE `Push_arguments` (
  `Push_id` bigint(20) NOT NULL,
  `arguments` varchar(255) DEFAULT NULL,
  `arguments_KEY` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`Push_id`,`arguments_KEY`),
  KEY `FKB6D4569142BD189E` (`Push_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 
*/