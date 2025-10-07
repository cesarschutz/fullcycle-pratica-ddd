import { IRepository } from "src/@core/common/domain/repository-interface";
import { Order } from "src/@core/events/domain/entities/order.entity";

// eslint-disable-next-line @typescript-eslint/no-empty-interface
export interface IOrderRepository extends IRepository<Order> {}
