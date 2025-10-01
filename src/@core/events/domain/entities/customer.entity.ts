import { AggregateRoot } from "src/@core/common/domain/aggregate-root";
import { Name } from "src/@core/common/domain/value-objects/name.vo";

export type CustomerConstructorProps = {
  id?: string;
  cpf: string;
  name: Name;
}

export class Customer extends AggregateRoot {
  id?: string;
  cpf: string;
  name: Name;

  constructor(props: CustomerConstructorProps) {
    super();
    this.id = props.id;
    this.cpf = props.cpf;
    this.name = props.name;
  }

  static create(command: {name: Name, cpf: string}) {
    return new Customer(command);
  }

  toJSON() {
    return {
      id: this.id,
      cpf: this.cpf,
      name: this.name,
    };
  }
}