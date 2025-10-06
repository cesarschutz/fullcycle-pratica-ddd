import { EntitySchema } from "@mikro-orm/core";
import { Partner } from "../../domain/entities/partner.entity";

export const PartnerSchema = new EntitySchema<Partner>({
    class: Partner,
    properties: {
      id: { primary: true, type: 'uuid' },
      name: { type: 'string', length: 255 },
    },
  });