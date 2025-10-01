import isEqual from 'lodash/isEqual';

/**
 * Classe base para Value Objects em Domain-Driven Design.
 *
 * Value Objects são objetos imutáveis que são definidos por seus atributos ao invés de uma identidade única.
 * Dois Value Objects com os mesmos atributos são considerados iguais.
 *
 * @template Value - O tipo do valor sendo encapsulado
 *
 * @example
 * ```typescript
 * class Email extends ValueObject<string> {
 *   constructor(email: string) {
 *     super(email);
 *   }
 * }
 *
 * const email1 = new Email('test@example.com');
 * const email2 = new Email('test@example.com');
 * console.log(email1.equals(email2)); // true
 * ```
 */
export abstract class ValueObject<Value = any> {
  /**
   * O valor interno imutável.
   * Protegido para permitir acesso em classes derivadas.
   */
  protected readonly _value: Value;

  /**
   * Cria uma nova instância de Value Object.
   * O valor é congelado profundamente para garantir imutabilidade.
   *
   * @param value - O valor a ser encapsulado
   */
  constructor(value: Value) {
    this._value = deepFreeze(value);
  }

  /**
   * Obtém o valor encapsulado.
   *
   * @returns O valor imutável
   */
  get value(): Value {
    return this._value;
  }

  /**
   * Compara este Value Object com outro para verificar igualdade.
   * Dois Value Objects são iguais se possuem o mesmo tipo e o mesmo valor.
   *
   * @param obj - O Value Object a ser comparado
   * @returns `true` se os Value Objects são iguais, `false` caso contrário
   */
  public equals(obj: this): boolean {
    if (obj === null || obj === undefined) {
      return false;
    }

    if (obj.value === undefined) {
      return false;
    }

    if (obj.constructor.name !== this.constructor.name) {
      return false;
    }

    return isEqual(this.value, obj.value);
  }

  /**
   * Converte o Value Object para uma representação em string.
   *
   * @returns Representação em string do valor
   */
  toString = () => {
    if (typeof this.value !== 'object' || this.value === null) {
      try {
        return this.value.toString();
      } catch (e) {
        return this.value + '';
      }
    }
    const valueStr = this.value.toString();
    return valueStr === '[object Object]'
      ? JSON.stringify(this.value)
      : valueStr;
  };
}

/**
 * Congela recursivamente um objeto e todas as suas propriedades aninhadas para garantir imutabilidade profunda.
 *
 * Isso é essencial para que os Value Objects mantenham sua natureza imutável.
 *
 * @template T - O tipo do objeto a ser congelado
 * @param obj - O objeto a ser congelado
 * @returns O objeto profundamente congelado
 *
 * @example
 * ```typescript
 * const obj = { nome: 'João', endereco: { cidade: 'SP' } };
 * const congelado = deepFreeze(obj);
 * congelado.nome = 'Maria'; // Erro: Não é possível atribuir à propriedade somente leitura
 * congelado.endereco.cidade = 'RJ'; // Erro: Não é possível atribuir à propriedade somente leitura
 * ```
 */
export function deepFreeze<T>(obj: T) {
  try {
    const propNames = Object.getOwnPropertyNames(obj);

    for (const name of propNames) {
      const value = obj[name as keyof T];

      if (value && typeof value === 'object') {
        deepFreeze(value);
      }
    }

    return Object.freeze(obj);
  } catch (e) {
    return obj;
  }
}
