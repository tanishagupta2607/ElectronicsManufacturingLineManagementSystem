package com.emls;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import javax.swing.table.JTableHeader;

public class MainFrame extends JFrame {
    private final Session session;
    private final JTabbedPane tabs = new JTabbedPane();

    public MainFrame(Session session) {
        super("EMLS — " + session.role() + " — " + session.username());
        this.session = session;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 760);
        setLocationRelativeTo(null);

        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        JLabel identity = new JLabel("Signed in as " + session.username() + " (" + session.role() + ")");
        identity.setFont(identity.getFont().deriveFont(Font.BOLD, 14f));
        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> { dispose(); new LoginFrame().setVisible(true); });
        top.add(identity, BorderLayout.WEST);
        top.add(logout, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);

        tabs.addTab("Dashboard", dashboardPanel());
        if (session.isAdmin()) {
            tabs.addTab("Products", productPanel(true));
            tabs.addTab("Inventory", inventoryPanel(true));
            tabs.addTab("Suppliers", genericPanel("SUPPLIER",
                    new String[]{"SUPPLIER_ID","SUPPLIER_NAME","CONTACT","EMAIL","ADDRESS"}, true));
            tabs.addTab("Production Lines", genericPanel("PRODUCTION_LINE",
                    new String[]{"LINE_ID","LINE_NAME","LOCATION","CAPACITY","STATUS"}, true));
            tabs.addTab("Employees", genericPanel("EMPLOYEE",
                    new String[]{"EMPLOYEE_ID","EMPLOYEE_NAME","ROLE","CONTACT","LINE_ID"}, true));
            tabs.addTab("Machines", machinePanel(true));
            tabs.addTab("Production", productionPanel(true));
            tabs.addTab("Maintenance", maintenancePanel(true));
            tabs.addTab("Orders", ordersPanel(true));
            tabs.addTab("Users", usersPanel());
            tabs.addTab("Reports", reportsPanel());
        } else if (session.isStaff()) {
            tabs.addTab("Products", productPanel(false));
            tabs.addTab("Inventory", inventoryPanel(false));
            tabs.addTab("Machines", machinePanel(false));
            tabs.addTab("Production", productionPanel(false));
            tabs.addTab("Maintenance", maintenancePanel(false));
            tabs.addTab("Orders", ordersPanel(false));
            tabs.addTab("Reports", reportsPanel());
        } else {
            tabs.addTab("Product Catalogue", clientCataloguePanel());
            tabs.addTab("My Orders", clientOrdersPanel());
        }
    }

    private JPanel dashboardPanel() {
    JPanel root = new JPanel(new BorderLayout(16, 20));
    root.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
    root.setBackground(new Color(245, 247, 251));

    JPanel heading = new JPanel(new BorderLayout(5, 5));
    heading.setOpaque(false);

    JLabel title = new JLabel(
        session.isClient() ? "Client Dashboard" : "Administrator Dashboard"
    );
    title.setFont(new Font("Segoe UI", Font.BOLD, 25));
    title.setForeground(new Color(23, 37, 84));

    JLabel subtitle = new JLabel(
        session.isClient()
            ? "Track your orders and their current status."
            : "Overview of your electronics manufacturing operations."
    );
    subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    subtitle.setForeground(new Color(100, 116, 139));

    heading.add(title, BorderLayout.NORTH);
    heading.add(subtitle, BorderLayout.CENTER);

    JPanel cards = new JPanel(new GridLayout(0, 3, 16, 16));
    cards.setOpaque(false);
    cards.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));

    try {
        if (session.isClient()) {
            addMetric(cards, "My Orders",
                count("SELECT COUNT(*) FROM CUSTOMER_ORDER WHERE CUSTOMER_ID = " + safeCustomerId()));

            addMetric(cards, "Open Orders",
                count("SELECT COUNT(*) FROM CUSTOMER_ORDER WHERE CUSTOMER_ID = " + safeCustomerId()
                    + " AND STATUS NOT IN ('COMPLETED','REJECTED')"));
        } else {
            addMetric(cards, "Total Products",
                count("SELECT COUNT(*) FROM PRODUCT"));

            addMetric(cards, "Production Lines",
                count("SELECT COUNT(*) FROM PRODUCTION_LINE"));

            addMetric(cards, "Employees",
                count("SELECT COUNT(*) FROM EMPLOYEE"));

            addMetric(cards, "Machines",
                count("SELECT COUNT(*) FROM MACHINE"));

            addMetric(cards, "Pending Orders",
                count("SELECT COUNT(*) FROM CUSTOMER_ORDER WHERE STATUS = 'PENDING'"));

            addMetric(cards, "Low-stock Components",
                count("SELECT COUNT(*) FROM COMPONENT WHERE QUANTITY <= 10"));

            addMetric(cards, "Machines Needing Attention",
                count("SELECT COUNT(*) FROM MACHINE WHERE UPPER(STATUS) NOT IN ('OPERATIONAL','ACTIVE')"));
        }
    } catch (Exception ex) {
        cards.add(new JLabel("Dashboard unavailable: " + ex.getMessage()));
    }

    JPanel content = new JPanel(new BorderLayout(10, 10));
    content.setOpaque(false);
    content.add(heading, BorderLayout.NORTH);
    content.add(cards, BorderLayout.CENTER);

    JLabel footer = new JLabel(
        "EMLS  |  Electronics Manufacturing Line Management System"
    );
    footer.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    footer.setForeground(new Color(100, 116, 139));

    JButton refresh = new JButton("Refresh Dashboard");
    refresh.setFocusPainted(false);
    refresh.addActionListener(e -> {
        tabs.setComponentAt(0, dashboardPanel());
        tabs.setSelectedIndex(0);
    });

    JPanel bottom = new JPanel(new BorderLayout());
    bottom.setOpaque(false);
    bottom.add(footer, BorderLayout.WEST);
    bottom.add(refresh, BorderLayout.EAST);

    root.add(content, BorderLayout.CENTER);
    root.add(bottom, BorderLayout.SOUTH);

    return root;
}
    private long safeCustomerId() { return session.customerId() == null ? -1 : session.customerId(); }
    private long count(String sql) throws SQLException { return Db.scalarLong(sql); }
    private void addMetric(JPanel p, String label, long value) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(210,215,220)),
                BorderFactory.createEmptyBorder(16,16,16,16)));
        JLabel v = new JLabel(Long.toString(value), SwingConstants.CENTER);
        v.setFont(v.getFont().deriveFont(Font.BOLD, 28f));
        JLabel l = new JLabel(label, SwingConstants.CENTER);
        card.add(v, BorderLayout.CENTER); card.add(l, BorderLayout.SOUTH); p.add(card);
    }

    private JPanel productPanel(boolean admin) {
        JPanel p = new JPanel(new BorderLayout(8,8));
        JTable table = new JTable();
        Runnable refresh = () -> runLoad(table, "SELECT PRODUCT_ID, PRODUCT_NAME, PRODUCT_TYPE, MODEL_NO, UNIT_PRICE FROM PRODUCT ORDER BY PRODUCT_ID");
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton reload = new JButton("Refresh");
        reload.addActionListener(e -> refresh.run()); buttons.add(reload);
        if (admin) {
            JButton add = new JButton("Add product");
            add.addActionListener(e -> {
                try {
                    String name = required("Product name");
                    String type = required("Product type");
                    String model = required("Model number");
                    double price = Double.parseDouble(required("Unit price"));
                    if (price <= 0) throw new IllegalArgumentException("Price must be greater than zero.");
                    try (Connection c=Config.connect(); PreparedStatement ps=c.prepareStatement(
                            "INSERT INTO PRODUCT(PRODUCT_ID,PRODUCT_NAME,PRODUCT_TYPE,MODEL_NO,UNIT_PRICE) VALUES(PRODUCT_SEQ.NEXTVAL,?,?,?,?)")) {
                        ps.setString(1,name); ps.setString(2,type); ps.setString(3,model); ps.setDouble(4,price); ps.executeUpdate();
                    }
                    refresh.run(); Db.info(this,"Product added.");
                } catch (Exception ex) { Db.error(this,ex); }
            }); buttons.add(add);
            JButton edit = new JButton("Update selected price");
            edit.addActionListener(e -> {
                Long id=Db.selectedId(table); if(id==null){Db.info(this,"Select a product first.");return;}
                try {
                    double price=Double.parseDouble(required("New unit price"));
                    if(price<=0) throw new IllegalArgumentException("Price must be greater than zero.");
                    try(Connection c=Config.connect();PreparedStatement ps=c.prepareStatement("UPDATE PRODUCT SET UNIT_PRICE=? WHERE PRODUCT_ID=?")){
                        ps.setDouble(1,price);ps.setLong(2,id);ps.executeUpdate();
                    } refresh.run();
                } catch(Exception ex){Db.error(this,ex);}
            }); buttons.add(edit);
            JButton delete = new JButton("Delete product");
delete.addActionListener(e -> {
    Long id = Db.selectedId(table);

    if (id == null) {
        Db.info(this, "Select a product first.");
        return;
    }

    int confirm = JOptionPane.showConfirmDialog(
        this,
        "Are you sure you want to delete product ID " + id + "?",
        "Confirm deletion",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.WARNING_MESSAGE
    );

    if (confirm != JOptionPane.YES_OPTION) {
        return;
    }

    try (Connection c = Config.connect()) {
        int productionCount;
        int orderCount;

        try (PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT(*) FROM PRODUCTION WHERE PRODUCT_ID = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                productionCount = rs.getInt(1);
            }
        }

        try (PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT(*) FROM ORDER_ITEM WHERE PRODUCT_ID = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                orderCount = rs.getInt(1);
            }
        }

        if (productionCount > 0 || orderCount > 0) {
            Db.info(this,
                "This product is linked to production or customer orders. "
                + "It cannot be deleted.");
            return;
        }

        try (PreparedStatement ps = c.prepareStatement(
                "DELETE FROM PRODUCT WHERE PRODUCT_ID = ?")) {
            ps.setLong(1, id);
            int deleted = ps.executeUpdate();

            if (deleted == 0) {
                Db.info(this, "Product not found.");
                return;
            }
        }

        refresh.run();
        Db.info(this, "Product deleted successfully.");

    } catch (Exception ex) {
        Db.error(this, ex);
    }
});
buttons.add(delete);
        }
        p.add(buttons, BorderLayout.SOUTH); refresh.run(); return p;
    }

    private JPanel clientCataloguePanel() {
        JPanel p = new JPanel(new BorderLayout(8,8));
        JTable table = new JTable();
        runLoad(table,"SELECT PRODUCT_ID, PRODUCT_NAME, PRODUCT_TYPE, MODEL_NO, UNIT_PRICE FROM PRODUCT ORDER BY PRODUCT_NAME");
        p.add(new JScrollPane(table),BorderLayout.CENTER);
        JPanel controls=new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refresh=new JButton("Refresh catalogue");
        refresh.addActionListener(e->runLoad(table,"SELECT PRODUCT_ID, PRODUCT_NAME, PRODUCT_TYPE, MODEL_NO, UNIT_PRICE FROM PRODUCT ORDER BY PRODUCT_NAME"));
        JButton order=new JButton("Place order for selected product");
        order.addActionListener(e->{
            Long productId=Db.selectedId(table);
            if(productId==null){Db.info(this,"Select a product first.");return;}
            try {
                int qty=Integer.parseInt(required("Quantity"));
                if(qty<1) throw new IllegalArgumentException("Quantity must be at least 1.");
                try(Connection c=Config.connect()){
                    c.setAutoCommit(false);
                    try {
                        double price;
                        try(PreparedStatement ps=c.prepareStatement("SELECT UNIT_PRICE FROM PRODUCT WHERE PRODUCT_ID=?")){
                            ps.setLong(1,productId);try(ResultSet rs=ps.executeQuery()){if(!rs.next())throw new SQLException("Product no longer exists.");price=rs.getDouble(1);}
                        }
                        long orderId;
                        try(PreparedStatement ps=c.prepareStatement(
                            "INSERT INTO CUSTOMER_ORDER(ORDER_ID,CUSTOMER_ID,ORDER_DATE,STATUS,TOTAL_AMOUNT) VALUES(CUSTOMER_ORDER_SEQ.NEXTVAL,?,SYSDATE,'PENDING',?)",
                            new String[]{"ORDER_ID"})){
                            ps.setLong(1,safeCustomerId());ps.setDouble(2,price*qty);ps.executeUpdate();
                            try(ResultSet keys=ps.getGeneratedKeys()){
                                if(keys.next()) orderId=keys.getLong(1);
                                else try(Statement st=c.createStatement();ResultSet rs=st.executeQuery("SELECT CUSTOMER_ORDER_SEQ.CURRVAL FROM DUAL")){rs.next();orderId=rs.getLong(1);}
                            }
                        }
                        try(PreparedStatement ps=c.prepareStatement("INSERT INTO ORDER_ITEM(ORDER_ITEM_ID,ORDER_ID,PRODUCT_ID,QUANTITY,UNIT_PRICE) VALUES(ORDER_ITEM_SEQ.NEXTVAL,?,?,?,?)")){
                            ps.setLong(1,orderId);ps.setLong(2,productId);ps.setInt(3,qty);ps.setDouble(4,price);ps.executeUpdate();
                        }
                        try(PreparedStatement ps=c.prepareStatement("INSERT INTO ORDER_STATUS_HISTORY(HISTORY_ID,ORDER_ID,STATUS,UPDATED_BY,NOTE,UPDATED_AT) VALUES(ORDER_STATUS_HISTORY_SEQ.NEXTVAL,?,'PENDING',?,'Order submitted by client',SYSDATE)")){
                            ps.setLong(1,orderId);ps.setLong(2,session.userId());ps.executeUpdate();
                        }
                        c.commit();Db.info(this,"Order #"+orderId+" submitted successfully.");
                    } catch(Exception ex){c.rollback();throw ex;} finally {c.setAutoCommit(true);}
                }
            }catch(Exception ex){Db.error(this,ex);}
        });
        controls.add(refresh);controls.add(order);p.add(controls,BorderLayout.SOUTH);return p;
    }

    private JPanel clientOrdersPanel() {
        JPanel p=new JPanel(new BorderLayout(8,8)); JTable table=new JTable();
        Runnable refresh=()->runLoad(table,"SELECT ORDER_ID, ORDER_DATE, STATUS, TOTAL_AMOUNT FROM CUSTOMER_ORDER WHERE CUSTOMER_ID=? ORDER BY ORDER_DATE DESC",safeCustomerId());
        p.add(new JScrollPane(table),BorderLayout.CENTER);
        JPanel b=new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton r=new JButton("Refresh");r.addActionListener(e->refresh.run());b.add(r);
        JButton timeline=new JButton("View selected order timeline");
        timeline.addActionListener(e->{Long id=Db.selectedId(table);if(id==null){Db.info(this,"Select an order.");return;}
            if(!ownsOrder(id)){Db.info(this,"Order not found.");return;}
            showTableDialog("Order status timeline","SELECT STATUS, UPDATED_AT, NOTE FROM ORDER_STATUS_HISTORY WHERE ORDER_ID=? ORDER BY UPDATED_AT",id);});
        b.add(timeline);p.add(b,BorderLayout.SOUTH);refresh.run();return p;
    }

    private boolean ownsOrder(long id) {
        try(Connection c=Config.connect();PreparedStatement ps=c.prepareStatement("SELECT COUNT(*) FROM CUSTOMER_ORDER WHERE ORDER_ID=? AND CUSTOMER_ID=?")){
            ps.setLong(1,id);ps.setLong(2,safeCustomerId());try(ResultSet rs=ps.executeQuery()){return rs.next()&&rs.getLong(1)>0;}
        }catch(Exception ex){Db.error(this,ex);return false;}
    }

    private JPanel inventoryPanel(boolean admin) {
        JPanel p=new JPanel(new BorderLayout(8,8)); JTable table=new JTable();
        Runnable refresh=()->runLoad(table,"SELECT COMPONENT_ID, COMPONENT_NAME, COMPONENT_TYPE, QUANTITY, UNIT_COST, SUPPLIER_ID FROM COMPONENT ORDER BY COMPONENT_ID");
        p.add(new JScrollPane(table),BorderLayout.CENTER);
        JPanel b=new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton r=new JButton("Refresh");r.addActionListener(e->refresh.run());b.add(r);
        JButton low=new JButton("Show low stock (<=10)");
        low.addActionListener(e->runLoad(table,"SELECT COMPONENT_ID, COMPONENT_NAME, COMPONENT_TYPE, QUANTITY, UNIT_COST, SUPPLIER_ID FROM COMPONENT WHERE QUANTITY <= 10 ORDER BY QUANTITY"));
        b.add(low);
        JButton update=new JButton("Update selected quantity");
        update.addActionListener(e->{Long id=Db.selectedId(table);if(id==null){Db.info(this,"Select a component.");return;}
            try{int q=Integer.parseInt(required("New quantity"));if(q<0)throw new IllegalArgumentException("Quantity cannot be negative.");
                try(Connection c=Config.connect();PreparedStatement ps=c.prepareStatement("UPDATE COMPONENT SET QUANTITY=? WHERE COMPONENT_ID=?")){
                    ps.setInt(1,q);ps.setLong(2,id);ps.executeUpdate();}
                refresh.run();Db.info(this,"Inventory updated.");
            }catch(Exception ex){Db.error(this,ex);}});
        b.add(update);
        if(admin){JButton add=new JButton("Add component");add.addActionListener(e->{
            try{String name=required("Component name"),type=required("Component type");int qty=Integer.parseInt(required("Quantity"));
                double cost=Double.parseDouble(required("Unit cost"));long supplier=Long.parseLong(required("Supplier ID"));
                try(Connection c=Config.connect();PreparedStatement ps=c.prepareStatement("INSERT INTO COMPONENT(COMPONENT_ID,COMPONENT_NAME,COMPONENT_TYPE,QUANTITY,UNIT_COST,SUPPLIER_ID) VALUES(COMPONENT_SEQ.NEXTVAL,?,?,?,?,?)")){
                    ps.setString(1,name);ps.setString(2,type);ps.setInt(3,qty);ps.setDouble(4,cost);ps.setLong(5,supplier);ps.executeUpdate();}
                refresh.run();
            }catch(Exception ex){Db.error(this,ex);}
        });b.add(add);}
        p.add(b,BorderLayout.SOUTH);refresh.run();return p;
    }

    private JPanel machinePanel(boolean admin) {
        JPanel p=new JPanel(new BorderLayout(8,8));JTable table=new JTable();
        Runnable refresh=()->runLoad(table,"SELECT MACHINE_ID, MACHINE_NAME, MACHINE_TYPE, STATUS, LINE_ID FROM MACHINE ORDER BY MACHINE_ID");
        p.add(new JScrollPane(table),BorderLayout.CENTER);JPanel b=new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton r=new JButton("Refresh");r.addActionListener(e->refresh.run());b.add(r);
        JButton fault=new JButton("Report fault / update status");
        fault.addActionListener(e->{Long id=Db.selectedId(table);if(id==null){Db.info(this,"Select a machine.");return;}
            try{String status=required("Status (Operational / Maintenance / Fault)");
                try(Connection c=Config.connect();PreparedStatement ps=c.prepareStatement("UPDATE MACHINE SET STATUS=? WHERE MACHINE_ID=?")){
                    ps.setString(1,status);ps.setLong(2,id);ps.executeUpdate();}
                refresh.run();Db.info(this,"Machine status updated.");
            }catch(Exception ex){Db.error(this,ex);}});
        b.add(fault);p.add(b,BorderLayout.SOUTH);refresh.run();return p;
    }

    private JPanel productionPanel(boolean admin) {
        JPanel p=new JPanel(new BorderLayout(8,8));JTable table=new JTable();
        Runnable refresh=()->runLoad(table,"SELECT PRODUCTION_ID, PRODUCT_ID, LINE_ID, PRODUCTION_DATE, QUANTITY, STATUS FROM PRODUCTION ORDER BY PRODUCTION_DATE DESC");
        p.add(new JScrollPane(table),BorderLayout.CENTER);JPanel b=new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton r=new JButton("Refresh");r.addActionListener(e->refresh.run());b.add(r);
        JButton add=new JButton("Record production");
        add.addActionListener(e->{try{
            long product=Long.parseLong(required("Product ID"));long line=Long.parseLong(required("Line ID"));
            int qty=Integer.parseInt(required("Quantity"));String status=required("Status (IN_PROGRESS / COMPLETED)");
            try(Connection c=Config.connect();PreparedStatement ps=c.prepareStatement("INSERT INTO PRODUCTION(PRODUCTION_ID,PRODUCT_ID,LINE_ID,PRODUCTION_DATE,QUANTITY,STATUS) VALUES(PRODUCTION_SEQ.NEXTVAL,?,?,SYSDATE,?,?)")){
                ps.setLong(1,product);ps.setLong(2,line);ps.setInt(3,qty);ps.setString(4,status.toUpperCase());ps.executeUpdate();}
            refresh.run();Db.info(this,"Production record saved.");
        }catch(Exception ex){Db.error(this,ex);}});
        b.add(add);
        JButton update=new JButton("Update selected status");
        update.addActionListener(e->{Long id=Db.selectedId(table);if(id==null){Db.info(this,"Select a production record.");return;}
            try{String status=required("New status");try(Connection c=Config.connect();PreparedStatement ps=c.prepareStatement("UPDATE PRODUCTION SET STATUS=? WHERE PRODUCTION_ID=?")){
                ps.setString(1,status.toUpperCase());ps.setLong(2,id);ps.executeUpdate();}refresh.run();}
            catch(Exception ex){Db.error(this,ex);}});
        b.add(update);p.add(b,BorderLayout.SOUTH);refresh.run();return p;
    }

    private JPanel maintenancePanel(boolean admin) {
        JPanel p=new JPanel(new BorderLayout(8,8));JTable table=new JTable();
        Runnable refresh=()->runLoad(table,"SELECT MAINTENANCE_ID, MACHINE_ID, MAINTENANCE_DATE, DESCRIPTION, COST FROM MAINTENANCE ORDER BY MAINTENANCE_DATE DESC");
        p.add(new JScrollPane(table),BorderLayout.CENTER);JPanel b=new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton r=new JButton("Refresh");r.addActionListener(e->refresh.run());b.add(r);
        JButton add=new JButton("Record maintenance");
        add.addActionListener(e->{try{long machine=Long.parseLong(required("Machine ID"));String desc=required("Description");double cost=Double.parseDouble(required("Cost"));
            try(Connection c=Config.connect();PreparedStatement ps=c.prepareStatement("INSERT INTO MAINTENANCE(MAINTENANCE_ID,MACHINE_ID,MAINTENANCE_DATE,DESCRIPTION,COST) VALUES(MAINTENANCE_SEQ.NEXTVAL,?,SYSDATE,?,?)")){
                ps.setLong(1,machine);ps.setString(2,desc);ps.setDouble(3,cost);ps.executeUpdate();}
            try(Connection c=Config.connect();PreparedStatement ps=c.prepareStatement("UPDATE MACHINE SET STATUS='Operational' WHERE MACHINE_ID=?")){
                ps.setLong(1,machine);ps.executeUpdate();}
            refresh.run();Db.info(this,"Maintenance saved. Machine marked Operational.");
        }catch(Exception ex){Db.error(this,ex);}});
        b.add(add);p.add(b,BorderLayout.SOUTH);refresh.run();return p;
    }

    private JPanel ordersPanel(boolean admin) {
        JPanel p=new JPanel(new BorderLayout(8,8));JTable table=new JTable();
        String sql="SELECT ORDER_ID,CUSTOMER_ID,ORDER_DATE,STATUS,TOTAL_AMOUNT FROM CUSTOMER_ORDER ORDER BY ORDER_DATE DESC";
        Runnable refresh=()->runLoad(table,sql);
        p.add(new JScrollPane(table),BorderLayout.CENTER);JPanel b=new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton r=new JButton("Refresh");r.addActionListener(e->refresh.run());b.add(r);
        JButton details=new JButton("Order items");
        details.addActionListener(e->{Long id=Db.selectedId(table);if(id==null){Db.info(this,"Select an order.");return;}
            showTableDialog("Order items","SELECT OI.ORDER_ITEM_ID, P.PRODUCT_NAME, OI.QUANTITY, OI.UNIT_PRICE FROM ORDER_ITEM OI JOIN PRODUCT P ON P.PRODUCT_ID=OI.PRODUCT_ID WHERE OI.ORDER_ID=?",id);});
        b.add(details);
        JButton status=new JButton("Update order status");
        status.addActionListener(e->{Long id=Db.selectedId(table);if(id==null){Db.info(this,"Select an order.");return;}
            try{String st=required("New status: APPROVED / REJECTED / IN_PRODUCTION / READY / COMPLETED");
                st=st.toUpperCase().trim();
                if(!java.util.Set.of("APPROVED","REJECTED","IN_PRODUCTION","READY","COMPLETED").contains(st))
                    throw new IllegalArgumentException("Choose one of the listed statuses.");
                try(Connection c=Config.connect()){
                    c.setAutoCommit(false);
                    try(PreparedStatement ps=c.prepareStatement("UPDATE CUSTOMER_ORDER SET STATUS=? WHERE ORDER_ID=?")){
                        ps.setString(1,st);ps.setLong(2,id);ps.executeUpdate();}
                    try(PreparedStatement ps=c.prepareStatement("INSERT INTO ORDER_STATUS_HISTORY(HISTORY_ID,ORDER_ID,STATUS,UPDATED_BY,NOTE,UPDATED_AT) VALUES(ORDER_STATUS_HISTORY_SEQ.NEXTVAL,?,?,?, ?,SYSDATE)")){
                        ps.setLong(1,id);ps.setString(2,st);ps.setLong(3,session.userId());ps.setString(4,"Status updated by "+session.username());ps.executeUpdate();}
                    c.commit();
                }
                refresh.run();Db.info(this,"Order status updated.");
            }catch(Exception ex){Db.error(this,ex);}});
        b.add(status);p.add(b,BorderLayout.SOUTH);refresh.run();return p;
    }

    private JPanel usersPanel() {
        JPanel p=new JPanel(new BorderLayout(8,8));JTable table=new JTable();
        Runnable refresh=()->runLoad(table,"SELECT USER_ID, USERNAME, ROLE, CUSTOMER_ID, ACTIVE, CREATED_AT FROM USER_ACCOUNT ORDER BY USER_ID");
        p.add(new JScrollPane(table),BorderLayout.CENTER);JPanel b=new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton r=new JButton("Refresh");r.addActionListener(e->refresh.run());b.add(r);
        JButton create=new JButton("Create staff account");
        create.addActionListener(e->{try{String username=required("Username");char[] pw=required("Temporary password").toCharArray();
            new AuthService().createUser(username,pw,"STAFF",null);java.util.Arrays.fill(pw,'\0');refresh.run();Db.info(this,"Staff account created.");}
            catch(Exception ex){Db.error(this,ex);}});
        b.add(create);
        
JButton createClient = new JButton("Create Client Account");

createClient.addActionListener(e -> {
    char[] password = null;

    try {
        String customerName = required("Client full name");
        String email = required("Client email");
        String phone = JOptionPane.showInputDialog(
        this, "Client phone (optional)");
String address = JOptionPane.showInputDialog(
        this, "Client address (optional)");
        String username = required("Client login username");

        JPasswordField passwordField = new JPasswordField(18);
        int choice = JOptionPane.showConfirmDialog(
            this,
            passwordField,
            "Set client password",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (choice != JOptionPane.OK_OPTION) {
            return;
        }

        password = passwordField.getPassword();

        if (password.length < 8) {
            throw new IllegalArgumentException(
                "Password must contain at least 8 characters."
            );
        }

        if (customerName.isBlank()
                || email.isBlank()
                || username.isBlank()) {
            throw new IllegalArgumentException(
                "Name, email and username are required."
            );
        }

        new AuthService().createClientAccount(
            customerName,
            email,
            phone,
            address,
            username,
            password
        );

        refresh.run();

        Db.info(this,
            "Client account created successfully. " +
            "The client can now log in.");

    } catch (Exception ex) {
        Db.error(this, ex);
    } finally {
        if (password != null) {
            java.util.Arrays.fill(password, '\0');
        }
    }
});

b.add(createClient);

        JButton toggle=new JButton("Activate/deactivate selected user");
        toggle.addActionListener(e->{Long id=Db.selectedId(table);if(id==null){Db.info(this,"Select a user.");return;}
            try{String active=required("Enter Y to activate or N to deactivate").toUpperCase();
                if(!active.equals("Y")&&!active.equals("N"))throw new IllegalArgumentException("Enter Y or N.");
                if(id==session.userId()&&active.equals("N"))throw new IllegalArgumentException("You cannot deactivate your own current account.");
                try(Connection c=Config.connect();PreparedStatement ps=c.prepareStatement("UPDATE USER_ACCOUNT SET ACTIVE=? WHERE USER_ID=?")){
                    ps.setString(1,active);ps.setLong(2,id);ps.executeUpdate();}refresh.run();}
            catch(Exception ex){Db.error(this,ex);}});
        b.add(toggle);p.add(b,BorderLayout.SOUTH);refresh.run();return p;
    }

    private JPanel reportsPanel() {
        JPanel p=new JPanel(new BorderLayout(8,8));JTable table=new JTable();
        p.add(new JScrollPane(table),BorderLayout.CENTER);JPanel b=new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton production=new JButton("Production by line");
        production.addActionListener(e->runLoad(table,"SELECT PL.LINE_NAME, SUM(PR.QUANTITY) TOTAL_QUANTITY FROM PRODUCTION PR JOIN PRODUCTION_LINE PL ON PL.LINE_ID=PR.LINE_ID GROUP BY PL.LINE_NAME ORDER BY TOTAL_QUANTITY DESC"));
        b.add(production);
        JButton inventory=new JButton("Low-stock report");
        inventory.addActionListener(e->runLoad(table,"SELECT COMPONENT_ID, COMPONENT_NAME, QUANTITY, UNIT_COST FROM COMPONENT WHERE QUANTITY<=10 ORDER BY QUANTITY"));
        b.add(inventory);
        JButton maint=new JButton("Maintenance cost");
        maint.addActionListener(e->runLoad(table,"SELECT M.MACHINE_NAME, COUNT(MA.MAINTENANCE_ID) JOBS, NVL(SUM(MA.COST),0) TOTAL_COST FROM MACHINE M LEFT JOIN MAINTENANCE MA ON MA.MACHINE_ID=M.MACHINE_ID GROUP BY M.MACHINE_NAME ORDER BY TOTAL_COST DESC"));
        b.add(maint);
        JButton orders=new JButton("Orders by status");
        orders.addActionListener(e->runLoad(table,"SELECT STATUS, COUNT(*) ORDER_COUNT, NVL(SUM(TOTAL_AMOUNT),0) TOTAL_VALUE FROM CUSTOMER_ORDER GROUP BY STATUS ORDER BY STATUS"));
        b.add(orders);
        p.add(b,BorderLayout.SOUTH);return p;
    }

    private JPanel genericPanel(String tableName, String[] columns, boolean admin) {
        JPanel p=new JPanel(new BorderLayout(8,8));JTable table=new JTable();
        String sql="SELECT "+String.join(",",columns)+" FROM "+tableName+" ORDER BY "+columns[0];
        Runnable refresh=()->runLoad(table,sql);
        p.add(new JScrollPane(table),BorderLayout.CENTER);
        JPanel b=new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton r=new JButton("Refresh");r.addActionListener(e->refresh.run());b.add(r);
        JButton note=new JButton("Manage via Oracle SQL for full record edits");
        note.setEnabled(false);b.add(note);
        p.add(b,BorderLayout.SOUTH);refresh.run();return p;
    }

    private String required(String prompt) {
        String value=Db.input(this,prompt);
        if(value==null)throw new IllegalArgumentException("Operation cancelled.");
        value=value.trim();if(value.isEmpty())throw new IllegalArgumentException(prompt+" is required.");
        return value;
    }


private void runLoad(JTable table, String sql, Object... params) {
    try {
        Db.loadTable(table, sql, params);
        styleTable(table);
    } catch (Exception ex) {
        Db.error(this, ex);
    }
}

private void styleTable(JTable table) {
    Color navy = new Color(23, 37, 84);
    Color blue = new Color(219, 234, 254);
    Color grid = new Color(226, 232, 240);

    table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    table.setRowHeight(32);
    table.setFillsViewportHeight(true);
    table.setShowGrid(true);
    table.setGridColor(grid);
    table.setIntercellSpacing(new Dimension(1, 1));
    table.setSelectionMode(
        ListSelectionModel.SINGLE_SELECTION
    );
    table.setSelectionBackground(blue);
    table.setSelectionForeground(navy);
    table.setAutoCreateRowSorter(true);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

JTableHeader header = table.getTableHeader();
header.setFont(new Font("Segoe UI", Font.BOLD, 12));
header.setOpaque(true);
header.setBackground(navy);
header.setForeground(Color.WHITE);
header.setPreferredSize(new Dimension(0, 36));
header.setReorderingAllowed(false);

header.setDefaultRenderer((tbl, value, isSelected, hasFocus, row, column) -> {
    JLabel label = new JLabel(value == null ? "" : value.toString());
    label.setOpaque(true);
    label.setBackground(navy);
    label.setForeground(Color.WHITE);
    label.setFont(new Font("Segoe UI", Font.BOLD, 12));
    label.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
    return label;
});
}


    private void showTableDialog(String title,String sql,Object... params) {
        JTable t=new JTable();runLoad(t,sql,params);
        JDialog d=new JDialog(this,title,true);d.setSize(650,350);d.setLocationRelativeTo(this);
        d.add(new JScrollPane(t));d.setVisible(true);
    }
}
