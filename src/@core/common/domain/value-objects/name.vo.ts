import { ValueObject } from "./value-object";

export class Name extends ValueObject<String> {
    constructor(name: string) {
        super(name);
        this.isValid();
      }
    
      isValid() {
        return this.value.length >= 3;
      }
}