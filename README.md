# FastUM
This Java application, is a robust backend system designed for managing restaurant operations, order processing, and performance tracking. It follows a clean architectural pattern, separating data access, business logic, and a console-based user interface.

### **Core Modules & Architecture**

The project is structured into three primary layers:

* **Data Layer (FastDL)**: Implements the Data Access Object (DAO) pattern to handle persistence. It includes specialized DAOs for users, orders (`Pedido`), restaurants, and menu items, all managed through a central configuration.
* **Logic Layer (FastLN)**: The "heart" of the application, organized into two sub-systems:
    * **SubSistemaPedidos**: Manages the lifecycle of an order, including menus, products, ingredients, and customization options.
    * **SubSistemaGestao**: Focused on administrative tasks, user management (Employees, Managers, Gestors), and operational metrics.
* **User Interface (FastUI)**: A comprehensive Model-View-Controller (MVC) implementation for a command-line interface. It provides tailored views and controllers for different user roles like Clients, Employees, and Managers.

### **Key Functionalities**

* **Order Lifecycle Management**: Handles everything from order creation via `PedidoBuilder` to state tracking (`EstadoPedido`) and kitchen task management (`Tarefa`).
* **Inventory & Customization**: Supports complex product structures where items can be customized with specific ingredients, while the system tracks stock levels in real-time.
* **Business Intelligence & Metrics**: The `ServicoMetricas` provides data-driven insights such as billing information (`Faturacao`), average product preparation times, and identifying the most requested workstations.
* **Authentication & Role Management**: A secure session system (`IniciarSessaoController`) ensures that users only access functionalities relevant to their roles, such as the `COOView` or `GerenteView`.

### **Technical Stack**

* **Language**: Java.
* **Build System**: Gradle (Kotlin DSL).
* **Database**: MySQL (via `mysql-connector-java`).
* **Testing**: JUnit for unit testing core components like `Pedido`, `Produto`, and `Stock`.
* **Design Patterns**: Facade (Facade pattern used for system entries), DAO, MVC, and Builder.
